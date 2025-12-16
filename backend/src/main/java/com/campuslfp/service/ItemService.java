package com.campuslfp.service;

import com.campuslfp.dto.ItemDto;
import com.campuslfp.model.*;
import com.campuslfp.repository.ItemRepository;
import com.campuslfp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    // Create new item
    public Item createItem(ItemDto dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Item item = Item.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .type(dto.getType())
            .category(dto.getCategory())
            .tags(dto.getTags())
            .location(dto.getLocation())
            .imageUrl(dto.getImageUrl())
            .dateReported(Instant.now())
            .status(ItemStatus.OPEN)
            .postedBy(user)
            .build();

        return itemRepository.save(item);
    }

    // List/filter items
    public List<Item> getItems(Optional<ItemType> type, Optional<String> category, Optional<String> location) {
        List<Item> items = itemRepository.findAll();
        return items.stream()
            .filter(i -> type.map(t -> i.getType() == t).orElse(true))
            .filter(i -> category.map(c -> c.equalsIgnoreCase(i.getCategory())).orElse(true))
            .filter(i -> location.map(l -> l.equalsIgnoreCase(i.getLocation())).orElse(true))
            .collect(Collectors.toList());
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    /**
     * Update an item. Owners can update their own items (basic fields).
     * Admins may update all fields including status and matched flag.
     *
     * @param id item id
     * @param dto incoming DTO
     * @param userEmail requestor email
     * @param isAdmin whether requestor has admin role
     */
    public Item updateItem(Long id, ItemDto dto, String userEmail, boolean isAdmin) {
        Item item = getItemById(id);
        // If not owner and not admin -> unauthorized
        if (!isAdmin && !item.getPostedBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        // Common fields owners and admins can update
        if (dto.getTitle() != null) item.setTitle(dto.getTitle());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getCategory() != null) item.setCategory(dto.getCategory());
        if (dto.getTags() != null) item.setTags(dto.getTags());
        if (dto.getLocation() != null) item.setLocation(dto.getLocation());
        if (dto.getImageUrl() != null) item.setImageUrl(dto.getImageUrl());

        // Admin-only updates
        if (isAdmin) {
            if (dto.getStatus() != null) item.setStatus(dto.getStatus());
            if (dto.getMatched() != null) item.setMatched(dto.getMatched());
            if (dto.getFlagged() != null) item.setFlagged(dto.getFlagged());
        }

        return itemRepository.save(item);
    }

    // Any authenticated user may flag an item for moderator review
    public Item flagItem(Long id, String userEmail) {
        Item item = getItemById(id);
        item.setFlagged(true);
        return itemRepository.save(item);
    }

    // Only admins may unflag (clear) an item
    public Item unflagItem(Long id, String userEmail, boolean isAdmin) {
        if (!isAdmin) throw new RuntimeException("Unauthorized");
        Item item = getItemById(id);
        item.setFlagged(false);
        return itemRepository.save(item);
    }

    // Admins can get all flagged items
    public List<Item> getFlaggedItems() {
        return itemRepository.findByFlaggedTrue();
    }

    public void deleteItem(Long id, String userEmail) {
        Item item = getItemById(id);
        if (!item.getPostedBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        itemRepository.delete(item);
    }

    // Improved matching:
    // - normalize tags (comma/semicolon separated), lowercase and trim
    // - exclude the same item, items already marked matched, closed items, and items by same user
    // - compute a simple score: tag overlap count + category match + location match
    // - return candidates with score > 0 sorted by score desc then recency
    public List<Item> findMatches(Long itemId) {
        Item item = getItemById(itemId);
        ItemType oppositeType = item.getType() == ItemType.LOST ? ItemType.FOUND : ItemType.LOST;
        List<Item> candidates = itemRepository.findByType(oppositeType);

        // helper to split and normalize tags
        java.util.function.Function<String, Set<String>> normalizeTags = (s) -> {
            if (s == null || s.isBlank()) return Collections.emptySet();
            return Arrays.stream(s.split("[,;]"))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toSet());
        };

        Set<String> tagSet = normalizeTags.apply(item.getTags());

        // Compute score for each candidate
        List<Item> results = new ArrayList<>();
        Map<Long, Integer> scoreMap = new HashMap<>();

        for (Item c : candidates) {
            // skip the same item
            if (c.getId().equals(item.getId())) continue;
            // skip items already matched
            if (Boolean.TRUE.equals(c.getMatched())) continue;
            // skip non-open items
            if (c.getStatus() != null && c.getStatus() != ItemStatus.OPEN) continue;
            // skip items posted by same user
            if (item.getPostedBy() != null && c.getPostedBy() != null &&
                    Objects.equals(item.getPostedBy().getId(), c.getPostedBy().getId())) continue;

            int score = 0;

            // category match
            if (item.getCategory() != null && c.getCategory() != null &&
                    item.getCategory().equalsIgnoreCase(c.getCategory())) {
                score += 2; // category is important
            }

            // location match (simple equality)
            if (item.getLocation() != null && c.getLocation() != null &&
                    item.getLocation().equalsIgnoreCase(c.getLocation())) {
                score += 1;
            }

            // tag overlap
            Set<String> candidateTags = normalizeTags.apply(c.getTags());
            Set<String> intersection = new HashSet<>(candidateTags);
            intersection.retainAll(tagSet);
            score += intersection.size();

            if (score > 0) {
                results.add(c);
                scoreMap.put(c.getId(), score);
            }
        }

        // sort by score desc, then newer first
        results.sort((a, b) -> {
            int sa = scoreMap.getOrDefault(a.getId(), 0);
            int sb = scoreMap.getOrDefault(b.getId(), 0);
            if (sb != sa) return Integer.compare(sb, sa);
            Instant ia = a.getDateReported() == null ? Instant.EPOCH : a.getDateReported();
            Instant ib = b.getDateReported() == null ? Instant.EPOCH : b.getDateReported();
            return ib.compareTo(ia);
        });

        return results;
    }
}
