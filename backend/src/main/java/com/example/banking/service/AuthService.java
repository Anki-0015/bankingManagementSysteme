package com.example.banking.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.banking.dto.AuthDtos.AuthResponse;
import com.example.banking.dto.AuthDtos.ForgotPasswordRequest;
import com.example.banking.dto.AuthDtos.LoginRequest;
import com.example.banking.dto.AuthDtos.ResetPasswordRequest;
import com.example.banking.dto.AuthDtos.SignupRequest;
import com.example.banking.model.PasswordResetToken;
import com.example.banking.model.Role;
import com.example.banking.model.User;
import com.example.banking.repository.PasswordResetTokenRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.JwtUtil;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final Random random = new SecureRandom();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil, PasswordResetTokenRepository tokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setBalance(BigDecimal.ZERO);
    user.setRole(Role.USER);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername());
    return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getBalance(), user.getRole() != null ? user.getRole().name() : null);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            // rethrow for handler to convert to 401
            throw e;
        } catch (Exception e) {
            // Wrap unexpected auth errors
            throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
        }
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getBalance(), user.getRole() != null ? user.getRole().name() : null);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) return; // do not reveal existence
        User user = userOpt.get();
        String rawToken = generateNumericOtp();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(rawToken);
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        tokenRepository.save(token);
        emailService.sendSimpleMessage(user.getEmail(), "Password Reset OTP", "Your OTP is: " + rawToken + " (valid 10 minutes)");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired or used");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        token.setUsed(true);
        userRepository.save(user);
    }

    private String generateNumericOtp() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
