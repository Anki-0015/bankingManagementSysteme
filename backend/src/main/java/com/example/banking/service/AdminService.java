package com.example.banking.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.banking.model.Role;
import com.example.banking.model.User;
import com.example.banking.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User setBalance(Long id, BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance must be non-negative");
        }
        User u = userRepository.findById(id).orElseThrow();
        u.setBalance(balance.setScale(2, java.math.RoundingMode.HALF_UP));
        return u;
    }

    @Transactional
    public User setRole(Long id, Role role) {
        if (role == null) throw new IllegalArgumentException("Role required");
        User u = userRepository.findById(id).orElseThrow();
        u.setRole(role);
        return u;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) return; // idempotent
        userRepository.deleteById(id);
    }

    @Transactional
    public User createUser(String username, String email, String password, Role role) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        if (password == null || password.length() < 4) throw new IllegalArgumentException("Password too short");
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("Username taken");
        if (userRepository.existsByEmail(email)) throw new IllegalArgumentException("Email already used");
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setBalance(BigDecimal.ZERO);
        u.setRole(role == null ? Role.USER : role);
        return userRepository.save(u);
    }
}
