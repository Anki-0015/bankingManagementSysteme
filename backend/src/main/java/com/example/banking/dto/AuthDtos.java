package com.example.banking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

        public static class SignupRequest {
                @NotBlank @Size(min = 3, max = 50)
                private String username;
                @Email @NotBlank
                private String email;
                @NotBlank @Size(min = 6, max = 100)
                private String password;
                public SignupRequest() {}
                public SignupRequest(String username, String email, String password) { this.username = username; this.email = email; this.password = password; }
                public String getUsername() { return username; }
                public void setUsername(String username) { this.username = username; }
                public String getEmail() { return email; }
                public void setEmail(String email) { this.email = email; }
                public String getPassword() { return password; }
                public void setPassword(String password) { this.password = password; }
        }

        public static class LoginRequest {
                @NotBlank
                private String username;
                @NotBlank
                private String password;
                public LoginRequest() {}
                public LoginRequest(String username, String password) { this.username = username; this.password = password; }
                public String getUsername() { return username; }
                public void setUsername(String username) { this.username = username; }
                public String getPassword() { return password; }
                public void setPassword(String password) { this.password = password; }
        }

        public static class AuthResponse {
                private String token;
                private String username;
                private String email;
                private BigDecimal balance;
                private String role; // expose role so frontend can gate admin UI
                public AuthResponse() {}
                public AuthResponse(String token, String username, String email, BigDecimal balance) { this(token, username, email, balance, null); }
                public AuthResponse(String token, String username, String email, BigDecimal balance, String role) { this.token = token; this.username = username; this.email = email; this.balance = balance; this.role = role; }
                public String getToken() { return token; }
                public void setToken(String token) { this.token = token; }
                public String getUsername() { return username; }
                public void setUsername(String username) { this.username = username; }
                public String getEmail() { return email; }
                public void setEmail(String email) { this.email = email; }
                public BigDecimal getBalance() { return balance; }
                public void setBalance(BigDecimal balance) { this.balance = balance; }
                public String getRole() { return role; }
                public void setRole(String role) { this.role = role; }
        }

        public static class ForgotPasswordRequest {
                @Email @NotBlank
                private String email;
                public ForgotPasswordRequest() {}
                public ForgotPasswordRequest(String email) { this.email = email; }
                public String getEmail() { return email; }
                public void setEmail(String email) { this.email = email; }
        }

        public static class ResetPasswordRequest {
                @NotBlank
                private String token;
                @NotBlank
                private String newPassword;
                public ResetPasswordRequest() {}
                public ResetPasswordRequest(String token, String newPassword) { this.token = token; this.newPassword = newPassword; }
                public String getToken() { return token; }
                public void setToken(String token) { this.token = token; }
                public String getNewPassword() { return newPassword; }
                public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        }

        public static class AmountBase {
                @NotBlank
                private String amount; // keep as string, will parse to BigDecimal for validation
                public AmountBase() {}
                public AmountBase(String amount) { this.amount = amount; }
                public String getAmount() { return amount; }
                public void setAmount(String amount) { this.amount = amount; }
        }

        public static class DepositRequest extends AmountBase { public DepositRequest() {} public DepositRequest(String amount) { super(amount); } }
        public static class WithdrawRequest extends AmountBase { public WithdrawRequest() {} public WithdrawRequest(String amount) { super(amount); } }

        public static class TransferRequest extends AmountBase {
                @NotBlank
                private String targetUsername;
                public TransferRequest() {}
                public TransferRequest(String targetUsername, String amount) { super(amount); this.targetUsername = targetUsername; }
                public String getTargetUsername() { return targetUsername; }
                public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }
        }

        public static class TransactionResponse {
                private Long id;
                private String type;
                private String amount;
                private String description;
                private LocalDateTime createdAt;
                public TransactionResponse() {}
                public TransactionResponse(Long id, String type, String amount, String description, LocalDateTime createdAt) { this.id = id; this.type = type; this.amount = amount; this.description = description; this.createdAt = createdAt; }
                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }
                public String getType() { return type; }
                public void setType(String type) { this.type = type; }
                public String getAmount() { return amount; }
                public void setAmount(String amount) { this.amount = amount; }
                public String getDescription() { return description; }
                public void setDescription(String description) { this.description = description; }
                public LocalDateTime getCreatedAt() { return createdAt; }
                public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        }
}
