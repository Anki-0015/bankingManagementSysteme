package com.example.banking.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.banking.model.Role;
import com.example.banking.service.AdminService;

import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) { this.adminService = adminService; }

    public record UserSummary(Long id, String username, String email, BigDecimal balance, Role role) {}
    public record BalanceUpdateRequest(@Min(0) BigDecimal balance) {}
    public record RoleUpdateRequest(Role role) {}
    public record CreateUserRequest(String username, String email, String password, Role role) {}

    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers().stream()
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getEmail(), u.getBalance(), u.getRole()))
                .toList());
    }

    @PatchMapping("/users/{id}/balance")
    public ResponseEntity<UserSummary> updateBalance(@PathVariable Long id, @RequestBody BalanceUpdateRequest req) {
        var u = adminService.setBalance(id, req.balance());
        return ResponseEntity.ok(new UserSummary(u.getId(), u.getUsername(), u.getEmail(), u.getBalance(), u.getRole()));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserSummary> updateRole(@PathVariable Long id, @RequestBody RoleUpdateRequest req) {
        var u = adminService.setRole(id, req.role());
        return ResponseEntity.ok(new UserSummary(u.getId(), u.getUsername(), u.getEmail(), u.getBalance(), u.getRole()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users")
    public ResponseEntity<UserSummary> createUser(@RequestBody CreateUserRequest req) {
        var u = adminService.createUser(req.username(), req.email(), req.password(), req.role());
        return ResponseEntity.ok(new UserSummary(u.getId(), u.getUsername(), u.getEmail(), u.getBalance(), u.getRole()));
    }
}
