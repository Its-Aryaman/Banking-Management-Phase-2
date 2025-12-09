package com.example.demo.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.models.Account;
import com.example.demo.repository.AccountRepository;

@Service
public class AccountService {
  private final AccountRepository repo;

  public AccountService(AccountRepository repo) { this.repo = repo; }

  public Account create(Account a) {
    if(repo.existsByAccountNumber(a.getAccountNumber())) throw new IllegalArgumentException("exists");
    return repo.save(a);
  }

  public Account getByAccountNumber(String accountNumber) {
    return repo.findByAccountNumber(accountNumber)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public Account updateBalance(String accountNumber, double newBalance) {
    Account a = getByAccountNumber(accountNumber);
    a.setBalance(newBalance);
    return repo.save(a);
  }

  public Account changeStatus(String accountNumber, boolean active) {
    Account a = getByAccountNumber(accountNumber);
    return repo.save(a);
  }
}

