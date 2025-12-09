package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.models.Account;

public interface AccountRepository extends MongoRepository<Account, String> {
	  Optional<Account> findByAccountNumber(String accountNumber);
	  boolean existsByAccountNumber(String accountNumber);
	}