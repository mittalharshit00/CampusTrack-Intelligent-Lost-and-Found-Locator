package com.campuslfp.service;

import com.campuslfp.dto.request.ItemCreateRequest;
import com.campuslfp.dto.request.ItemUpdateRequest;
import com.campuslfp.dto.response.ItemDto;
import com.campuslfp.enums.ItemStatus;
import com.campuslfp.enums.ItemType;
import com.campuslfp.exception.BadRequestException;
import com.campuslfp.exception.ResourceNotFoundException;
import com.campuslfp.mapper.ItemMapper;
import com.campuslfp.model.Item;
import com.campuslfp.model.User;
import com.campuslfp.repository.ItemRepository;
import com.campuslfp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    public Item createItem(ItemCreateRequest request, MultipartFile image, String userEmail) {
        validateImage(image);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ItemDto dto = itemMapper.toDto(request);
        dto.setImageUrl(toDataUri(image));

        Item item = itemMapper.toEntity(dto);
        item.setPostedBy(user);
        return itemRepository.save(item);
    }

    public List<Item> getItems(Optional<ItemType> type, Optional<String> category, Optional<String> location) {
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .filter(item -> type.map(selectedType -> item.getType() == selectedType).orElse(true))
                .filter(item -> category.map(selectedCategory -> selectedCategory.equalsIgnoreCase(item.getCategory()))
                        .orElse(true))
                .filter(item -> location.map(selectedLocation -> selectedLocation.equalsIgnoreCase(item.getLocation()))
                        .orElse(true))
                .collect(Collectors.toList());
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }

    public Item updateItem(Long id, ItemUpdateRequest request, Authentication authentication) {
        Item item = getItemById(id);
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");

        if (!isAdmin && !Objects.equals(item.getPostedBy().getEmail(), authentication.getName())) {
            throw new BadRequestException("Unauthorized");
        }

        ItemDto dto = itemMapper.toDto(request);

        if (isAdmin) {
            itemMapper.updateAdminFields(item, dto);
            return itemRepository.save(item);
        }

        itemMapper.updateUserFields(item, dto);
        return itemRepository.save(item);
    }

    public Item flagItem(Long id, Authentication authentication) {
        if (authentication == null) {
            throw new BadRequestException("Authentication required");
        }

        Item item = getItemById(id);
        item.setFlagged(true);
        return itemRepository.save(item);
    }

    public Item unflagItem(Long id, Authentication authentication) {
        if (authentication == null) {
            throw new BadRequestException("Authentication required");
        }
        if (!hasRole(authentication, "ROLE_ADMIN")) {
            throw new BadRequestException("Admin only");
        }
        Item item = getItemById(id);
        item.setFlagged(false);
        return itemRepository.save(item);
    }

    public List<Item> getFlaggedItems(Authentication authentication) {
        if (authentication == null || !hasRole(authentication, "ROLE_ADMIN")) {
            throw new BadRequestException("Admin only");
        }
        return itemRepository.findByFlaggedTrue();
    }

    public void deleteItem(Long id, Authentication authentication) {
        Item item = getItemById(id);
        if (!hasRole(authentication, "ROLE_ADMIN")
                && !Objects.equals(item.getPostedBy().getEmail(), authentication.getName())) {
            throw new BadRequestException("Unauthorized");
        }
        itemRepository.delete(item);
    }

    public List<Item> findMatches(Long itemId) {
        Item item = getItemById(itemId);
        ItemType oppositeType = item.getType() == ItemType.LOST ? ItemType.FOUND : ItemType.LOST;
        List<Item> candidateItems = itemRepository.findByType(oppositeType);

        java.util.function.Function<String, Set<String>> normalizeTags = value -> {
            if (value == null || value.isBlank()) {
                return Collections.emptySet();
            }
            return Arrays.stream(value.split("[,;]"))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
        };

        Set<String> itemTags = normalizeTags.apply(item.getTags());
        List<Item> matchedItems = new ArrayList<>();
        Map<Long, Integer> matchScores = new HashMap<>();

        for (Item candidate : candidateItems) {
            if (candidate.getId().equals(item.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(candidate.getMatched())) {
                continue;
            }
            if (candidate.getStatus() != null && candidate.getStatus() != ItemStatus.OPEN) {
                continue;
            }
            if (item.getPostedBy() != null && candidate.getPostedBy() != null &&
                    Objects.equals(item.getPostedBy().getId(), candidate.getPostedBy().getId())) {
                continue;
            }

            int score = 0;

            if (item.getCategory() != null && candidate.getCategory() != null &&
                    item.getCategory().equalsIgnoreCase(candidate.getCategory())) {
                score += 2;
            }

            if (item.getLocation() != null && candidate.getLocation() != null &&
                    item.getLocation().equalsIgnoreCase(candidate.getLocation())) {
                score += 1;
            }

            Set<String> candidateTags = normalizeTags.apply(candidate.getTags());
            Set<String> tagIntersection = new HashSet<>(candidateTags);
            tagIntersection.retainAll(itemTags);
            score += tagIntersection.size();

            if (score > 0) {
                matchedItems.add(candidate);
                matchScores.put(candidate.getId(), score);
            }
        }

        matchedItems.sort((left, right) -> {
            int leftScore = matchScores.getOrDefault(left.getId(), 0);
            int rightScore = matchScores.getOrDefault(right.getId(), 0);
            if (rightScore != leftScore) {
                return Integer.compare(rightScore, leftScore);
            }
            Instant leftReported = left.getDateReported() == null ? Instant.EPOCH : left.getDateReported();
            Instant rightReported = right.getDateReported() == null ? Instant.EPOCH : right.getDateReported();
            return rightReported.compareTo(leftReported);
        });

        return matchedItems;
    }

    public Item markItemsAsMatched(Long itemId, Long matchedItemId, String userEmail) {
        return markItemsAsMatched(itemId, matchedItemId, new UsernamePasswordAuthenticationToken(userEmail, null));
    }

    public Item markItemsAsMatched(Long itemId, Long matchedItemId, Authentication authentication) {
        if (authentication == null) {
            throw new BadRequestException("Authentication required");
        }

        Item primaryItem = getItemById(itemId);
        Item matchedItem = getItemById(matchedItemId);

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean userOwnsPrimaryItem = Objects.equals(primaryItem.getPostedBy().getId(), currentUser.getId());
        boolean userOwnsMatchedItem = Objects.equals(matchedItem.getPostedBy().getId(), currentUser.getId());
        if (!userOwnsPrimaryItem && !userOwnsMatchedItem) {
            throw new BadRequestException("Only the item owner can mark items as matched");
        }
        if (primaryItem.getType() == matchedItem.getType()) {
            throw new BadRequestException("A lost item can only be matched with a found item, and vice versa");
        }
        if (Boolean.TRUE.equals(primaryItem.getMatched()) || Boolean.TRUE.equals(matchedItem.getMatched())) {
            throw new BadRequestException("One or both items are already marked as matched");
        }
        if ((primaryItem.getStatus() == ItemStatus.CLOSED) || (matchedItem.getStatus() == ItemStatus.CLOSED)) {
            throw new BadRequestException("Closed items cannot be matched");
        }

        primaryItem.setMatched(true);
        primaryItem.setStatus(ItemStatus.MATCHED);
        primaryItem.setFlagged(false);

        matchedItem.setMatched(true);
        matchedItem.setStatus(ItemStatus.MATCHED);
        matchedItem.setFlagged(false);

        itemRepository.save(primaryItem);
        itemRepository.save(matchedItem);

        return primaryItem;
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("Image is required");
        }

        String contentType = image.getContentType();
        if (contentType == null
                || !("image/jpeg".equalsIgnoreCase(contentType) || "image/jpg".equalsIgnoreCase(contentType))) {
            throw new BadRequestException("Invalid image type. Only JPG/JPEG allowed.");
        }

        long maxSize = 5L * 1024L * 1024L;
        if (image.getSize() > maxSize) {
            throw new BadRequestException("Image size exceeds 5MB limit.");
        }
    }

    private String toDataUri(MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            String contentType = image.getContentType();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return "data:" + contentType + ";base64," + base64;
        } catch (Exception exc) {
            throw new BadRequestException("Failed to process image");
        }
    }
}
