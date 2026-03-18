package com.example.banking.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.banking.dto.AuthDtos.DepositRequest;
import com.example.banking.dto.AuthDtos.TransactionResponse;
import com.example.banking.dto.AuthDtos.TransferRequest;
import com.example.banking.dto.AuthDtos.WithdrawRequest;
import com.example.banking.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) { this.accountService = accountService; }

    private String username(Authentication auth) { return auth.getName(); }

    @PostMapping("/deposit")
    public ResponseEntity<BigDecimal> deposit(Authentication auth, @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(accountService.deposit(username(auth), request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<BigDecimal> withdraw(Authentication auth, @Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(accountService.withdraw(username(auth), request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<BigDecimal> transfer(Authentication auth, @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(username(auth), request));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> transactions(Authentication auth) {
        return ResponseEntity.ok(accountService.listTransactions(username(auth)));
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> balance(Authentication auth) {
        return ResponseEntity.ok(accountService.getBalance(username(auth)));
    }
}
