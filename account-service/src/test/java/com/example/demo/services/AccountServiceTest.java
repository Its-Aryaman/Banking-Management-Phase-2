
package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.models.Account;
import com.example.demo.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repo;

    @InjectMocks
    private AccountService accountService;

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
    void testCreate_Success() {

        when(repo.existsByAccountNumber(testAccount.getAccountNumber())).thenReturn(false);
        when(repo.save(any(Account.class))).thenReturn(testAccount);


        Account result = accountService.create(testAccount);


        assertNotNull(result);
        assertEquals("ACC001", result.getAccountNumber());
        assertEquals("John Doe", result.getHolderName());
        assertEquals(1000.0, result.getBalance());
        verify(repo, times(1)).existsByAccountNumber("ACC001");
        verify(repo, times(1)).save(testAccount);
    }

    @Test
    void testCreate_AccountAlreadyExists_ThrowsException() {

        when(repo.existsByAccountNumber(testAccount.getAccountNumber())).thenReturn(true);


        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.create(testAccount)
        );
        
        assertEquals("exists", exception.getMessage());
        verify(repo, times(1)).existsByAccountNumber("ACC001");
        verify(repo, never()).save(any(Account.class));
    }

    @Test
    void testGetByAccountNumber_Success() {

        when(repo.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));


        Account result = accountService.getByAccountNumber("ACC001");


        assertNotNull(result);
        assertEquals("ACC001", result.getAccountNumber());
        assertEquals("John Doe", result.getHolderName());
        verify(repo, times(1)).findByAccountNumber("ACC001");
    }

    @Test
    void testGetByAccountNumber_NotFound_ThrowsException() {

        when(repo.findByAccountNumber("INVALID")).thenReturn(Optional.empty());


        assertThrows(ResponseStatusException.class, 
            () -> accountService.getByAccountNumber("INVALID")
        );
        verify(repo, times(1)).findByAccountNumber("INVALID");
    }

    @Test
    void testUpdateBalance_Success() {

        double newBalance = 1500.0;
        Account updatedAccount = new Account();
        updatedAccount.setId("123");
        updatedAccount.setAccountNumber("ACC001");
        updatedAccount.setHolderName("John Doe");
        updatedAccount.setBalance(newBalance);
        updatedAccount.setActive(true);

        when(repo.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));
        when(repo.save(any(Account.class))).thenReturn(updatedAccount);


        Account result = accountService.updateBalance("ACC001", newBalance);


        assertNotNull(result);
        assertEquals(newBalance, result.getBalance());
        verify(repo, times(1)).findByAccountNumber("ACC001");
        verify(repo, times(1)).save(any(Account.class));
    }

    @Test
    void testUpdateBalance_AccountNotFound_ThrowsException() {

        when(repo.findByAccountNumber("INVALID")).thenReturn(Optional.empty());


        assertThrows(ResponseStatusException.class,
            () -> accountService.updateBalance("INVALID", 2000.0)
        );
        verify(repo, times(1)).findByAccountNumber("INVALID");
        verify(repo, never()).save(any(Account.class));
    }

    @Test
    void testUpdateBalance_NegativeBalance() {

        double negativeBalance = -500.0;
        Account updatedAccount = new Account();
        updatedAccount.setId("123");
        updatedAccount.setAccountNumber("ACC001");
        updatedAccount.setHolderName("John Doe");
        updatedAccount.setBalance(negativeBalance);
        updatedAccount.setActive(true);

        when(repo.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));
        when(repo.save(any(Account.class))).thenReturn(updatedAccount);


        Account result = accountService.updateBalance("ACC001", negativeBalance);


        assertNotNull(result);
        assertEquals(negativeBalance, result.getBalance());
        verify(repo, times(1)).save(any(Account.class));
    }

    @Test
    void testChangeStatus_Success() {

        when(repo.findByAccountNumber("ACC001")).thenReturn(Optional.of(testAccount));
        when(repo.save(any(Account.class))).thenReturn(testAccount);


        Account result = accountService.changeStatus("ACC001", false);


        assertNotNull(result);
        verify(repo, times(1)).findByAccountNumber("ACC001");
        verify(repo, times(1)).save(testAccount);
    }

    @Test
    void testChangeStatus_AccountNotFound_ThrowsException() {

        when(repo.findByAccountNumber("INVALID")).thenReturn(Optional.empty());


        assertThrows(ResponseStatusException.class,
            () -> accountService.changeStatus("INVALID", false)
        );
        verify(repo, times(1)).findByAccountNumber("INVALID");
        verify(repo, never()).save(any(Account.class));
    }
}