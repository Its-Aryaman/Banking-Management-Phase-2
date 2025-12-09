package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.models.Transaction;




public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findBySourceAccountOrDestinationAccountOrderByTimestampDesc(
            String source, String destination
    );
}
