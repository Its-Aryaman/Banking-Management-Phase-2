package com.example.demo.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.clients.AccountClient;
import com.example.demo.clients.AccountDTO;
import com.example.demo.clients.NotificationPayload;
import com.example.demo.clients.NotificationClient;
import com.example.demo.models.Transaction;
import com.example.demo.repository.TransactionRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class TransactionService {

    private final TransactionRepository txnRepo;
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public TransactionService(TransactionRepository txnRepo,
                              AccountClient accountClient,
                              NotificationClient notificationClient) {
        this.txnRepo = txnRepo;
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }


    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        return txnRepo.findBySourceAccountOrDestinationAccountOrderByTimestampDesc(
                accountNumber, accountNumber
        );
    }


    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackTransaction")
    public Transaction deposit(String accountNumber, double amount) {

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        AccountDTO acc = accountClient.getAccount(accountNumber);

        double newBalance = acc.getBalance() + amount;

        accountClient.updateBalance(accountNumber, Map.of("balance", newBalance));

        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + UUID.randomUUID());
        t.setType("DEPOSIT");
        t.setAmount(amount);
        t.setTimestamp(Instant.now());
        t.setStatus("SUCCESS");

        txnRepo.save(t);

        notificationClient.sendNotification(
                new NotificationPayload(
                        t.getTransactionId(),
                        "Deposit of " + amount + " completed.",
                        acc.getHolderName()
                )
        );

        return t;
    }

    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackTransaction")
    public Transaction withdraw(String accountNumber, double amount) {

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        AccountDTO acc = accountClient.getAccount(accountNumber);

        if (acc.getBalance() < amount)
            throw new IllegalArgumentException("Insufficient funds");

        double newBalance = acc.getBalance() - amount;

        accountClient.updateBalance(accountNumber, Map.of("balance", newBalance));

        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + UUID.randomUUID());
        t.setType("WITHDRAW");
        t.setAmount(amount);
        t.setTimestamp(Instant.now());
        t.setStatus("SUCCESS");

        txnRepo.save(t);

        notificationClient.sendNotification(
                new NotificationPayload(
                        t.getTransactionId(),
                        "Withdrawal of " + amount + " completed.",
                        acc.getHolderName()
                )
        );

        return t;
    }


    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackTransactionTransfer")
    public Transaction transfer(String fromAccount, String toAccount, double amount) {

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        if (fromAccount.equals(toAccount))
            throw new IllegalArgumentException("Cannot transfer to same account");

        AccountDTO source = accountClient.getAccount(fromAccount);
        AccountDTO dest = accountClient.getAccount(toAccount);

        if (source.getBalance() < amount)
            throw new IllegalArgumentException("Insufficient balance");

        accountClient.updateBalance(fromAccount, Map.of("balance", source.getBalance() - amount));


        accountClient.updateBalance(toAccount, Map.of("balance", dest.getBalance() + amount));

        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + UUID.randomUUID());
        t.setType("TRANSFER");
        t.setAmount(amount);
        t.setTimestamp(Instant.now());
        t.setStatus("SUCCESS");
        t.setSourceAccount(fromAccount);
        t.setDestinationAccount(toAccount);

        txnRepo.save(t);


        notificationClient.sendNotification(
                new NotificationPayload(
                        t.getTransactionId(),
                        "You sent ₹" + amount + " to " + toAccount,
                        source.getHolderName()
                )
        );

        notificationClient.sendNotification(
                new NotificationPayload(
                        t.getTransactionId(),
                        "You received ₹" + amount + " from " + fromAccount,
                        dest.getHolderName()
                )
        );

        return t;
    }

    public Transaction fallbackTransaction(String accountNumber, double amount, Throwable ex) {

        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + UUID.randomUUID());
        t.setTimestamp(Instant.now());
        t.setStatus("FAILED");
        t.setAmount(amount);

        txnRepo.save(t);
        return t;
    }

    public Transaction fallbackTransactionTransfer(String from, String to, double amount, Throwable ex) {

        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + UUID.randomUUID());
        t.setTimestamp(Instant.now());
        t.setStatus("FAILED");
        t.setSourceAccount(from);
        t.setDestinationAccount(to);
        t.setAmount(amount);

        txnRepo.save(t);
        return t;
    }
}
