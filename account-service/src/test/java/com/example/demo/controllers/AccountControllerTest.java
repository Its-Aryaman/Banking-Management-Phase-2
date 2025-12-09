
package com.example.demo.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import com.example.demo.models.Account;
import com.example.demo.services.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId("123");
        testAccount.setAccountNumber("ACC001");
        testAccount.setHolderName("John Doe");
        testAccount.setBalance(1000.0);
        testAccount.setActive(true);
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        // Arrange
        when(accountService.create(any(Account.class))).thenReturn(testAccount);

        // Act & Assert
        mockMvc.perform(post("/api/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("ACC001"))
                .andExpect(jsonPath("$.holderName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.active").value(true));

        verify(accountService, times(1)).create(any(Account.class));
    }

    @Test
    void testCreateAccount_AlreadyExists_ThrowsException() throws Exception {

        when(accountService.create(any(Account.class)))
            .thenThrow(new IllegalArgumentException("exists"));


        mockMvc.perform(post("/api/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isBadRequest());

        verify(accountService, times(1)).create(any(Account.class));
    }

    @Test
    void testGetAccount_Success() throws Exception {

        when(accountService.getByAccountNumber("ACC001")).thenReturn(testAccount);


        mockMvc.perform(get("/api/accounts/ACC001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC001"))
                .andExpect(jsonPath("$.holderName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(1000.0));

        verify(accountService, times(1)).getByAccountNumber("ACC001");
    }

    @Test
    void testGetAccount_NotFound_ThrowsException() throws Exception {

        when(accountService.getByAccountNumber("INVALID"))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));


        mockMvc.perform(get("/api/accounts/INVALID"))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getByAccountNumber("INVALID");
    }

    @Test
    void testUpdateBalance_Success() throws Exception {

        Account updatedAccount = new Account();
        updatedAccount.setId("123");
        updatedAccount.setAccountNumber("ACC001");
        updatedAccount.setHolderName("John Doe");
        updatedAccount.setBalance(1500.0);
        updatedAccount.setActive(true);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("balance", 1500.0);

        when(accountService.updateBalance("ACC001", 1500.0)).thenReturn(updatedAccount);


        mockMvc.perform(put("/api/accounts/ACC001/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC001"))
                .andExpect(jsonPath("$.balance").value(1500.0));

        verify(accountService, times(1)).updateBalance("ACC001", 1500.0);
    }

    @Test
    void testUpdateBalance_AccountNotFound_ThrowsException() throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("balance", 1500.0);

        when(accountService.updateBalance("INVALID", 1500.0))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));


        mockMvc.perform(put("/api/accounts/INVALID/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).updateBalance("INVALID", 1500.0);
    }

    @Test
    void testUpdateBalance_WithIntegerValue() throws Exception {

        Account updatedAccount = new Account();
        updatedAccount.setId("123");
        updatedAccount.setAccountNumber("ACC001");
        updatedAccount.setHolderName("John Doe");
        updatedAccount.setBalance(2000.0);
        updatedAccount.setActive(true);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("balance", 2000); // Integer value

        when(accountService.updateBalance("ACC001", 2000.0)).thenReturn(updatedAccount);


        mockMvc.perform(put("/api/accounts/ACC001/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(2000.0));

        verify(accountService, times(1)).updateBalance("ACC001", 2000.0);
    }

    @Test
    void testCreateAccount_WithMissingFields() throws Exception {

        Account incompleteAccount = new Account();
        incompleteAccount.setAccountNumber("ACC002");


        when(accountService.create(any(Account.class))).thenReturn(incompleteAccount);


        mockMvc.perform(post("/api/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteAccount)))
                .andExpect(status().isCreated());

        verify(accountService, times(1)).create(any(Account.class));
    }

    @Test
    void testUpdateBalance_WithNegativeValue() throws Exception {

        Account updatedAccount = new Account();
        updatedAccount.setId("123");
        updatedAccount.setAccountNumber("ACC001");
        updatedAccount.setHolderName("John Doe");
        updatedAccount.setBalance(-100.0);
        updatedAccount.setActive(true);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("balance", -100.0);

        when(accountService.updateBalance("ACC001", -100.0)).thenReturn(updatedAccount);


        mockMvc.perform(put("/api/accounts/ACC001/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-100.0));

        verify(accountService, times(1)).updateBalance("ACC001", -100.0);
    }
}
