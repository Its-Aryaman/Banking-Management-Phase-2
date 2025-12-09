package com.example.demo.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.models.Transaction;
import com.example.demo.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setId("txn123");
        testTransaction.setTransactionId("TXN-12345");
        testTransaction.setType("DEPOSIT");
        testTransaction.setAmount(1000.0);
        testTransaction.setTimestamp(Instant.now());
        testTransaction.setStatus("SUCCESS");
    }


    @Test
    void testDeposit_Success() throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountNumber", "ACC001");
        requestBody.put("amount", 1000.0);

        when(transactionService.deposit("ACC001", 1000.0)).thenReturn(testTransaction);


        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-12345"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(transactionService, times(1)).deposit("ACC001", 1000.0);
    }

    @Test
    void testDeposit_WithIntegerAmount() throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountNumber", "ACC001");
        requestBody.put("amount", 1000); // Integer instead of double

        when(transactionService.deposit("ACC001", 1000.0)).thenReturn(testTransaction);


        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1000.0));

        verify(transactionService, times(1)).deposit("ACC001", 1000.0);
    }


    @Test
    void testWithdraw_Success() throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountNumber", "ACC001");
        requestBody.put("amount", 500.0);

        Transaction withdrawTransaction = new Transaction();
        withdrawTransaction.setTransactionId("TXN-67890");
        withdrawTransaction.setType("WITHDRAW");
        withdrawTransaction.setAmount(500.0);
        withdrawTransaction.setStatus("SUCCESS");

        when(transactionService.withdraw("ACC001", 500.0)).thenReturn(withdrawTransaction);


        mockMvc.perform(post("/api/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-67890"))
                .andExpect(jsonPath("$.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.amount").value(500.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(transactionService, times(1)).withdraw("ACC001", 500.0);
    }



    @Test
    void testTransfer_Success() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("fromAccount", "ACC001");
        requestBody.put("toAccount", "ACC002");
        requestBody.put("amount", 1500.0);

        Transaction transferTransaction = new Transaction();
        transferTransaction.setTransactionId("TXN-TRANSFER-123");
        transferTransaction.setType("TRANSFER");
        transferTransaction.setAmount(1500.0);
        transferTransaction.setStatus("SUCCESS");
        transferTransaction.setSourceAccount("ACC001");
        transferTransaction.setDestinationAccount("ACC002");

        when(transactionService.transfer("ACC001", "ACC002", 1500.0))
                .thenReturn(transferTransaction);


        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-TRANSFER-123"))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(1500.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.sourceAccount").value("ACC001"))
                .andExpect(jsonPath("$.destinationAccount").value("ACC002"));

        verify(transactionService, times(1)).transfer("ACC001", "ACC002", 1500.0);
    }



    @Test
    void testTransfer_WithIntegerAmount() throws Exception {
 
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("fromAccount", "ACC001");
        requestBody.put("toAccount", "ACC002");
        requestBody.put("amount", 1000); // Integer

        Transaction transferTransaction = new Transaction();
        transferTransaction.setTransactionId("TXN-TRANSFER-456");
        transferTransaction.setType("TRANSFER");
        transferTransaction.setAmount(1000.0);
        transferTransaction.setStatus("SUCCESS");

        when(transactionService.transfer("ACC001", "ACC002", 1000.0))
                .thenReturn(transferTransaction);

        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1000.0));

        verify(transactionService, times(1)).transfer("ACC001", "ACC002", 1000.0);
    }

    @Test
    void testGetTransactions_Success() throws Exception {

        String accountNumber = "ACC001";
        
        Transaction txn1 = new Transaction();
        txn1.setTransactionId("TXN-1");
        txn1.setType("DEPOSIT");
        txn1.setAmount(1000.0);
        txn1.setStatus("SUCCESS");

        Transaction txn2 = new Transaction();
        txn2.setTransactionId("TXN-2");
        txn2.setType("WITHDRAW");
        txn2.setAmount(500.0);
        txn2.setStatus("SUCCESS");

        List<Transaction> transactions = Arrays.asList(txn1, txn2);

        when(transactionService.getTransactionsForAccount(accountNumber))
                .thenReturn(transactions);


        mockMvc.perform(get("/api/transactions/account/{number}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].transactionId").value("TXN-1"))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].amount").value(1000.0))
                .andExpect(jsonPath("$[1].transactionId").value("TXN-2"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAW"))
                .andExpect(jsonPath("$[1].amount").value(500.0));

        verify(transactionService, times(1)).getTransactionsForAccount(accountNumber);
    }

    @Test
    void testGetTransactions_EmptyList() throws Exception {
 
        String accountNumber = "ACC999";

        when(transactionService.getTransactionsForAccount(accountNumber))
                .thenReturn(Arrays.asList());

   
        mockMvc.perform(get("/api/transactions/account/{number}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(transactionService, times(1)).getTransactionsForAccount(accountNumber);
    }

    @Test
    void testGetTransactions_WithMultipleTransactionTypes() throws Exception {

        String accountNumber = "ACC001";
        
        Transaction deposit = new Transaction();
        deposit.setTransactionId("TXN-DEP");
        deposit.setType("DEPOSIT");
        deposit.setAmount(2000.0);

        Transaction withdraw = new Transaction();
        withdraw.setTransactionId("TXN-WITH");
        withdraw.setType("WITHDRAW");
        withdraw.setAmount(500.0);

        Transaction transfer = new Transaction();
        transfer.setTransactionId("TXN-TRANS");
        transfer.setType("TRANSFER");
        transfer.setAmount(1000.0);
        transfer.setSourceAccount("ACC001");
        transfer.setDestinationAccount("ACC002");

        List<Transaction> transactions = Arrays.asList(deposit, withdraw, transfer);

        when(transactionService.getTransactionsForAccount(accountNumber))
                .thenReturn(transactions);

   
        mockMvc.perform(get("/api/transactions/account/{number}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAW"))
                .andExpect(jsonPath("$[2].type").value("TRANSFER"))
                .andExpect(jsonPath("$[2].sourceAccount").value("ACC001"))
                .andExpect(jsonPath("$[2].destinationAccount").value("ACC002"));

        verify(transactionService, times(1)).getTransactionsForAccount(accountNumber);
    }
}