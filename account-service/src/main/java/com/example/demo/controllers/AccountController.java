package com.example.demo.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.Account;
import com.example.demo.services.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
  private final AccountService service;

  public AccountController(AccountService service) { this.service = service; }

  @PostMapping("/create")
  public ResponseEntity<Account> create(@RequestBody Account account) {
    Account saved = service.create(account);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping("/{accountNumber}")
  public Account get(@PathVariable String accountNumber) {
    return service.getByAccountNumber(accountNumber);
  }

  @PutMapping("/{accountNumber}/balance")
  public Account updateBalance(@PathVariable String accountNumber, @RequestBody Map<String,Object> body) {
    double newBalance = ((Number)body.get("balance")).doubleValue();
    return service.updateBalance(accountNumber, newBalance);
  }
}
