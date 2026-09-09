package com.campuslfp.controller;

import com.campuslfp.dto.request.ItemCreateRequest;
import com.campuslfp.dto.request.ItemUpdateRequest;
import com.campuslfp.dto.request.MatchItemRequest;
import com.campuslfp.enums.ItemType;
import com.campuslfp.model.Item;
import com.campuslfp.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<Item> createItem(
            @RequestPart("item") @Valid ItemCreateRequest request,
            @RequestPart("image") MultipartFile image,
            Authentication authentication) {

        return ResponseEntity.ok(itemService.createItem(request, image, authentication.getName()));
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
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @Valid @RequestBody ItemUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(itemService.updateItem(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id, Authentication authentication) {
        itemService.deleteItem(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<Item>> getSuggestedMatches(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findMatches(id));
    }

    @PostMapping("/{id}/flag")
    public ResponseEntity<Item> flagItem(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(itemService.flagItem(id, authentication));
    }

    @PostMapping("/{id}/match")
    public ResponseEntity<Item> markItemsAsMatched(
            @PathVariable Long id,
            @RequestBody MatchItemRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(itemService.markItemsAsMatched(id, request.getMatchedItemId(), authentication));
    }

    @DeleteMapping("/{id}/flag")
    public ResponseEntity<Item> unflagItem(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(itemService.unflagItem(id, authentication));
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<Item>> getFlaggedItems(Authentication authentication) {
        return ResponseEntity.ok(itemService.getFlaggedItems(authentication));
    }
}
