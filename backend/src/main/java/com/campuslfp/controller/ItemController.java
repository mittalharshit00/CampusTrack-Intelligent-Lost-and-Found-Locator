package com.campuslfp.controller;

import com.campuslfp.dto.ItemDto;
import com.campuslfp.model.Item;
import com.campuslfp.model.ItemType;
import com.campuslfp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<?> createItem(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            @RequestParam String location,
            @RequestParam(required = false) String color,
        @RequestParam MultipartFile image,
            Authentication authentication) {
        
        ItemDto dto = new ItemDto();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setType(ItemType.valueOf(type));
        dto.setCategory(category);
        dto.setTags(tags);
        dto.setLocation(location);
        dto.setColor(color);
        
        // Image is required for new items. Validate and convert to data URI (base64)
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Image is required"));
        }

        try {
            // Only accept JPEG/JPG (frontend limits this, but double-check on server)
            String contentType = image.getContentType();
            if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/jpg"))) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid image type. Only JPG/JPEG allowed."));
            }

            // Enforce max size 5MB
            long maxSize = 5L * 1024L * 1024L;
            if (image.getSize() > maxSize) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Image size exceeds 5MB limit."));
            }

            byte[] bytes = image.getBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String dataUri = "data:" + contentType + ";base64," + base64;
            dto.setImageUrl(dataUri);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Failed to process image"));
        }
        
        return ResponseEntity.ok(itemService.createItem(dto, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Item>> getItems(
            @RequestParam Optional<ItemType> type,
            @RequestParam Optional<String> category,
            @RequestParam Optional<String> location) {
        return ResponseEntity.ok(itemService.getItems(type, category, location));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemDto dto, Authentication authentication) {
        // Determine if the requester has admin role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try {
            Item updated = itemService.updateItem(id, dto, authentication.getName(), isAdmin);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id, Authentication authentication) {
        itemService.deleteItem(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // Suggested matches endpoint
    @GetMapping("/{id}/matches")
    public ResponseEntity<List<Item>> getMatches(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findMatches(id));
    }

    // Flag an item for moderator/admin review (any authenticated user)
    @PostMapping("/{id}/flag")
    public ResponseEntity<?> flagItem(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("message", "Authentication required"));
        }
        Item updated = itemService.flagItem(id, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    // Admin-only: unflag an item
    @DeleteMapping("/{id}/flag")
    public ResponseEntity<?> unflagItem(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", "Admin only"));
        }
        Item updated = itemService.unflagItem(id, authentication.getName(), isAdmin);
        return ResponseEntity.ok(updated);
    }

    // Admin-only: list flagged items
    @GetMapping("/flagged")
    public ResponseEntity<List<Item>> getFlaggedItems(Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(itemService.getFlaggedItems());
    }
}
