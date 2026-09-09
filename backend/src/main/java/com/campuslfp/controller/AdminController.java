package com.campuslfp.controller;

import com.campuslfp.model.Flag;
import com.campuslfp.model.Item;
import com.campuslfp.model.User;
import com.campuslfp.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/items")
    public List<Item> getAllItems() {
        return adminService.getAllItems();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        adminService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/flags")
    public List<Flag> getAllFlags() {
        return adminService.getAllFlags();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/pending")
    public List<User> getPendingUsers() {
        return adminService.getPendingUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/approve")
    public ResponseEntity<Void> approveUser(@PathVariable Long id) {
        adminService.approveUser(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/ignore")
    public ResponseEntity<Void> ignoreUser(@PathVariable Long id) {
        adminService.ignoreUser(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
        return ResponseEntity.ok().build();
    }
}
