package com.example.banking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.banking.dto.AuthDtos.DepositRequest;
import com.example.banking.dto.AuthDtos.TransactionResponse;
import com.example.banking.dto.AuthDtos.TransferRequest;
import com.example.banking.dto.AuthDtos.WithdrawRequest;
import com.example.banking.model.Transaction;
import com.example.banking.model.User;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public BigDecimal deposit(String username, DepositRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow();
        BigDecimal amount = parsePositiveAmount(request.getAmount());
        user.setBalance(user.getBalance().add(amount));
        recordTransaction(user, "DEPOSIT", amount, null);
        return user.getBalance();
    }

    @Transactional
    public BigDecimal withdraw(String username, WithdrawRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow();
        BigDecimal amount = parsePositiveAmount(request.getAmount());
        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        user.setBalance(user.getBalance().subtract(amount));
        recordTransaction(user, "WITHDRAW", amount, null);
        return user.getBalance();
    }

    @Transactional
    public BigDecimal transfer(String username, TransferRequest request) {
        if (username.equals(request.getTargetUsername())) {
            throw new IllegalArgumentException("Cannot transfer to self");
        }
        User sender = userRepository.findByUsername(username).orElseThrow();
        User target = userRepository.findByUsername(request.getTargetUsername()).orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        BigDecimal amount = parsePositiveAmount(request.getAmount());
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        sender.setBalance(sender.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));
        recordTransaction(sender, "TRANSFER_OUT", amount, target.getUsername());
        recordTransaction(target, "TRANSFER_IN", amount, sender.getUsername());
        return sender.getBalance();
    }

    public List<TransactionResponse> listTransactions(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return transactionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(t -> new TransactionResponse(
                        t.getId(), t.getType(), t.getAmount().toPlainString(), t.getDescription(), t.getCreatedAt()
                )).collect(Collectors.toList());
    }

    public BigDecimal getBalance(String username) {
        return userRepository.findByUsername(username).orElseThrow().getBalance();
    }

    private void recordTransaction(User user, String type, BigDecimal amount, String description) {
        Transaction t = new Transaction();
        t.setUser(user);
        t.setType(type);
        t.setAmount(amount);
        t.setDescription(description);
        transactionRepository.save(t);
    }

    private BigDecimal parsePositiveAmount(String value) {
        try {
            BigDecimal bd = new BigDecimal(value);
            if (bd.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
            return bd.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }
}
