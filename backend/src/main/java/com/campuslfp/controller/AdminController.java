package com.campuslfp.controller;

import com.campuslfp.model.Flag;
import com.campuslfp.model.Item;
import com.campuslfp.repository.FlagRepository;
import com.campuslfp.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ItemRepository itemRepository;
    private final FlagRepository flagRepository;
    private final com.campuslfp.repository.UserRepository userRepository;
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/items")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        itemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/flags")
    public List<Flag> getAllFlags() {
        return flagRepository.findAll();
    }

    // List users that require admin approval
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/pending")
    public List<com.campuslfp.model.User> getPendingUsers() {
        // prefer to hide ignored users; use custom repository method that treats NULL as not-ignored
        return userRepository.findPending();
    }

    // Approve a user account
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        var u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        u.setApproved(true);
        userRepository.save(u);
        return ResponseEntity.ok().build();
    }

    // Ignore a pending user so they are not shown in the pending list
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/ignore")
    public ResponseEntity<?> ignoreUser(@PathVariable Long id) {
        var u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        u.setIgnored(true);
        userRepository.save(u);
        return ResponseEntity.ok().build();
    }

    // List all users (for admin management)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<com.campuslfp.model.User> getAllUsers() {
        return userRepository.findAll();
    }

    // Block a user account (prevent login)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long id) {
        var u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        u.setBlocked(true);
        userRepository.save(u);
        return ResponseEntity.ok().build();
    }

    // Unblock a user account
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable Long id) {
        var u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        u.setBlocked(false);
        userRepository.save(u);
        return ResponseEntity.ok().build();
    }
}
