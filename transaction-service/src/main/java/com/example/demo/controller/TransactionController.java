package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.Transaction;
import com.example.demo.service.TransactionService;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/deposit")
    public Transaction deposit(@RequestBody Map<String,Object> body) {
        return service.deposit(
                (String) body.get("accountNumber"),
                ((Number) body.get("amount")).doubleValue()
        );
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(@RequestBody Map<String,Object> body) {
        return service.withdraw(
                (String) body.get("accountNumber"),
                ((Number) body.get("amount")).doubleValue()
        );
    }

    @PostMapping("/transfer")
    public Transaction transfer(@RequestBody Map<String,Object> body) {
        return service.transfer(
                (String) body.get("fromAccount"),
                (String) body.get("toAccount"),
                ((Number) body.get("amount")).doubleValue()
        );
    }

    @GetMapping("/account/{number}")
    public List<Transaction> getTxns(@PathVariable String number) {
        return service.getTransactionsForAccount(number);
    }
}
