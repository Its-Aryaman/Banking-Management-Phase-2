package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.clients.AccountClient;
import com.example.demo.clients.AccountDTO;
import com.example.demo.clients.NotificationClient;
import com.example.demo.clients.NotificationPayload;
import com.example.demo.models.Transaction;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.service.TransactionService;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository txnRepo;

    @Mock
    private AccountClient accountClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private TransactionService transactionService;

    private AccountDTO testAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testAccount = new AccountDTO();
        testAccount.setId("123");
        testAccount.setAccountNumber("ACC001");
        testAccount.setHolderName("John Doe");
        testAccount.setBalance(5000.0);

        testTransaction = new Transaction();
        testTransaction.setId("txn123");
        testTransaction.setTransactionId("TXN-uuid");
        testTransaction.setType("DEPOSIT");
        testTransaction.setAmount(1000.0);
        testTransaction.setTimestamp(Instant.now());
        testTransaction.setStatus("SUCCESS");
    }

    @Test
    void testGetTransactionsForAccount_Success() {

        String accountNumber = "ACC001";
        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);
        when(txnRepo.findBySourceAccountOrDestinationAccountOrderByTimestampDesc(
                accountNumber, accountNumber)).thenReturn(expectedTransactions);


        List<Transaction> result = transactionService.getTransactionsForAccount(accountNumber);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction, result.get(0));
        verify(txnRepo, times(1)).findBySourceAccountOrDestinationAccountOrderByTimestampDesc(
                accountNumber, accountNumber);
    }

    @Test
    void testGetTransactionsForAccount_EmptyList() {

        String accountNumber = "ACC002";
        when(txnRepo.findBySourceAccountOrDestinationAccountOrderByTimestampDesc(
                accountNumber, accountNumber)).thenReturn(Arrays.asList());


        List<Transaction> result = transactionService.getTransactionsForAccount(accountNumber);


        assertNotNull(result);
        assertEquals(0, result.size());
        verify(txnRepo, times(1)).findBySourceAccountOrDestinationAccountOrderByTimestampDesc(
                accountNumber, accountNumber);
    }



    @Test
    void testDeposit_Success() {

        String accountNumber = "ACC001";
        double depositAmount = 1000.0;
        double expectedNewBalance = 6000.0;

        when(accountClient.getAccount(accountNumber)).thenReturn(testAccount);
        when(accountClient.updateBalance(eq(accountNumber), anyMap())).thenReturn(testAccount);
        when(txnRepo.save(any(Transaction.class))).thenReturn(testTransaction);


        Transaction result = transactionService.deposit(accountNumber, depositAmount);


        assertNotNull(result);
        assertEquals("DEPOSIT", result.getType());
        assertEquals(depositAmount, result.getAmount());
        assertEquals("SUCCESS", result.getStatus());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("TXN-"));


        verify(accountClient, times(1)).getAccount(accountNumber);
        verify(accountClient, times(1)).updateBalance(eq(accountNumber), argThat(map ->
                map.get("balance").equals(expectedNewBalance)
        ));
        verify(txnRepo, times(1)).save(any(Transaction.class));
        verify(notificationClient, times(1)).sendNotification(any(NotificationPayload.class));
    }

    @Test
    void testDeposit_NegativeAmount_ThrowsException() {

        String accountNumber = "ACC001";
        double negativeAmount = -500.0;


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(accountNumber, negativeAmount)
        );

        assertEquals("Amount must be positive", exception.getMessage());
        verify(accountClient, never()).getAccount(anyString());
        verify(txnRepo, never()).save(any(Transaction.class));
        verify(notificationClient, never()).sendNotification(any(NotificationPayload.class));
    }

    @Test
    void testDeposit_ZeroAmount_ThrowsException() {

        String accountNumber = "ACC001";
        double zeroAmount = 0.0;


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(accountNumber, zeroAmount)
        );

        assertEquals("Amount must be positive", exception.getMessage());
        verify(accountClient, never()).getAccount(anyString());
    }

    @Test
    void testDeposit_NotificationSent_WithCorrectPayload() {

        String accountNumber = "ACC001";
        double depositAmount = 1000.0;

        when(accountClient.getAccount(accountNumber)).thenReturn(testAccount);
        when(accountClient.updateBalance(eq(accountNumber), anyMap())).thenReturn(testAccount);
        when(txnRepo.save(any(Transaction.class))).thenReturn(testTransaction);

        ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);


        transactionService.deposit(accountNumber, depositAmount);


        verify(notificationClient).sendNotification(captor.capture());
        NotificationPayload payload = captor.getValue();
        assertNotNull(payload);
        assertTrue(payload.getMessage().contains("Deposit"));
        assertTrue(payload.getMessage().contains(String.valueOf(depositAmount)));
        assertEquals(testAccount.getHolderName(), payload.getTo());
    }



    @Test
    void testWithdraw_Success() {

        String accountNumber = "ACC001";
        double withdrawAmount = 1000.0;
        double expectedNewBalance = 4000.0;

        when(accountClient.getAccount(accountNumber)).thenReturn(testAccount);
        when(accountClient.updateBalance(eq(accountNumber), anyMap())).thenReturn(testAccount);
        when(txnRepo.save(any(Transaction.class))).thenReturn(testTransaction);


        Transaction result = transactionService.withdraw(accountNumber, withdrawAmount);


        assertNotNull(result);
        assertEquals("WITHDRAW", result.getType());
        assertEquals(withdrawAmount, result.getAmount());
        assertEquals("SUCCESS", result.getStatus());

        verify(accountClient, times(1)).getAccount(accountNumber);
        verify(accountClient, times(1)).updateBalance(eq(accountNumber), argThat(map ->
                map.get("balance").equals(expectedNewBalance)
        ));
        verify(txnRepo, times(1)).save(any(Transaction.class));
        verify(notificationClient, times(1)).sendNotification(any(NotificationPayload.class));
    }

    @Test
    void testWithdraw_InsufficientFunds_ThrowsException() {

        String accountNumber = "ACC001";
        double withdrawAmount = 10000.0; 

        when(accountClient.getAccount(accountNumber)).thenReturn(testAccount);

 
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.withdraw(accountNumber, withdrawAmount)
        );

        assertEquals("Insufficient funds", exception.getMessage());
        verify(accountClient, times(1)).getAccount(accountNumber);
        verify(accountClient, never()).updateBalance(anyString(), anyMap());
        verify(txnRepo, never()).save(any(Transaction.class));
        verify(notificationClient, never()).sendNotification(any(NotificationPayload.class));
    }

    @Test
    void testWithdraw_NegativeAmount_ThrowsException() {

        String accountNumber = "ACC001";
        double negativeAmount = -500.0;


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.withdraw(accountNumber, negativeAmount)
        );

        assertEquals("Amount must be positive", exception.getMessage());
        verify(accountClient, never()).getAccount(anyString());
    }

    @Test
    void testWithdraw_ExactBalance_Success() {

        String accountNumber = "ACC001";
        double withdrawAmount = 5000.0; 
        double expectedNewBalance = 0.0;

        when(accountClient.getAccount(accountNumber)).thenReturn(testAccount);
        when(accountClient.updateBalance(eq(accountNumber), anyMap())).thenReturn(testAccount);
        when(txnRepo.save(any(Transaction.class))).thenReturn(testTransaction);


        Transaction result = transactionService.withdraw(accountNumber, withdrawAmount);


        assertNotNull(result);
        verify(accountClient, times(1)).updateBalance(eq(accountNumber), argThat(map ->
                map.get("balance").equals(expectedNewBalance)
        ));
    }


    @Test
    void testTransfer_Success() {

        String fromAccount = "ACC001";
        String toAccount = "ACC002";
        double transferAmount = 1000.0;

        AccountDTO sourceAccount = new AccountDTO("123", "ACC001", "John Doe", 5000.0);
        AccountDTO destAccount = new AccountDTO("456", "ACC002", "Jane Smith", 3000.0);

        when(accountClient.getAccount(fromAccount)).thenReturn(sourceAccount);
        when(accountClient.getAccount(toAccount)).thenReturn(destAccount);
        when(accountClient.updateBalance(eq(fromAccount), anyMap())).thenReturn(sourceAccount);
        when(accountClient.updateBalance(eq(toAccount), anyMap())).thenReturn(destAccount);
        when(txnRepo.save(any(Transaction.class))).thenReturn(testTransaction);


        Transaction result = transactionService.transfer(fromAccount, toAccount, transferAmount);


        assertNotNull(result);
        assertEquals("TRANSFER", result.getType());
        assertEquals(transferAmount, result.getAmount());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(fromAccount, result.getSourceAccount());
        assertEquals(toAccount, result.getDestinationAccount());

        verify(accountClient, times(1)).getAccount(fromAccount);
        verify(accountClient, times(1)).getAccount(toAccount);
        verify(accountClient, times(1)).updateBalance(eq(fromAccount), argThat(map ->
                map.get("balance").equals(4000.0)
        ));
        verify(accountClient, times(1)).updateBalance(eq(toAccount), argThat(map ->
                map.get("balance").equals(4000.0)
        ));
        verify(txnRepo, times(1)).save(any(Transaction.class));
        verify(notificationClient, times(2)).sendNotification(any(NotificationPayload.class));
    }

    @Test
    void testTransfer_SameAccount_ThrowsException() {

        String accountNumber = "ACC001";
        double amount = 1000.0;


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(accountNumber, accountNumber, amount)
        );

        assertEquals("Cannot transfer to same account", exception.getMessage());
        verify(accountClient, never()).getAccount(anyString());
        verify(txnRepo, never()).save(any(Transaction.class));
    }

    @Test
    void testTransfer_InsufficientBalance_ThrowsException() {

        String fromAccount = "ACC001";
        String toAccount = "ACC002";
        double transferAmount = 10000.0; 

        AccountDTO sourceAccount = new AccountDTO("123", "ACC001", "John Doe", 5000.0);
        AccountDTO destAccount = new AccountDTO("456", "ACC002", "Jane Smith", 3000.0);

        when(accountClient.getAccount(fromAccount)).thenReturn(sourceAccount);
        when(accountClient.getAccount(toAccount)).thenReturn(destAccount);


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(fromAccount, toAccount, transferAmount)
        );

        assertEquals("Insufficient balance", exception.getMessage());
        verify(accountClient, never()).updateBalance(anyString(), anyMap());
        verify(txnRepo, never()).save(any(Transaction.class));
    }

    @Test
    void testTransfer_NegativeAmount_ThrowsException() {

        String fromAccount = "ACC001";
        String toAccount = "ACC002";
        double negativeAmount = -500.0;


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(fromAccount, toAccount, negativeAmount)
        );

        assertEquals("Amount must be positive", exception.getMessage());
        verify(accountClient, never()).getAccount(anyString());
    }

    @Test
    void testTransfer_BothNotificationsSent() {

        String fromAccount = "ACC001";
        String toAccount = "ACC002";
        double transferAmount = 1000.0;

        AccountDTO sourceAccount = new AccountDTO("123", "ACC001", "John Doe", 5000.0);
        AccountDTO destAccount = new AccountDTO("456", "ACC002", "Jane Smith", 3000.0);

        when(accountClient.getAccount(fromAccount)).thenReturn(sourceAccount);
        when(accountClient.getAccount(toAccount)).thenReturn(destAccount);
        when(accountClient.updateBalance(anyString(), anyMap())).thenReturn(sourceAccount);
        when(txnRepo.save(any(Transaction.class))).thenReturn(testTransaction);

        ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);

 
        transactionService.transfer(fromAccount, toAccount, transferAmount);

    
        verify(notificationClient, times(2)).sendNotification(captor.capture());
        List<NotificationPayload> notifications = captor.getAllValues();

        assertEquals(2, notifications.size());
        
 
        NotificationPayload senderNotif = notifications.get(0);
        assertTrue(senderNotif.getMessage().contains("sent"));
        assertEquals("John Doe", senderNotif.getTo());


        NotificationPayload receiverNotif = notifications.get(1);
        assertTrue(receiverNotif.getMessage().contains("received"));
        assertEquals("Jane Smith", receiverNotif.getTo());
    }

    @Test
    void testFallbackTransaction_SavesFailedTransaction() {

        String accountNumber = "ACC001";
        double amount = 1000.0;
        Throwable exception = new RuntimeException("Service unavailable");

        when(txnRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        Transaction result = transactionService.fallbackTransaction(accountNumber, amount, exception);

    
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertEquals(amount, result.getAmount());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("TXN-"));
        assertNotNull(result.getTimestamp());

        verify(txnRepo, times(1)).save(any(Transaction.class));
    }

    @Test
    void testFallbackTransactionTransfer_SavesFailedTransfer() {
      
        String fromAccount = "ACC001";
        String toAccount = "ACC002";
        double amount = 1000.0;
        Throwable exception = new RuntimeException("Service unavailable");

        when(txnRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

  
        Transaction result = transactionService.fallbackTransactionTransfer(
                fromAccount, toAccount, amount, exception
        );

  
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertEquals(amount, result.getAmount());
        assertEquals(fromAccount, result.getSourceAccount());
        assertEquals(toAccount, result.getDestinationAccount());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("TXN-"));
        assertNotNull(result.getTimestamp());

        verify(txnRepo, times(1)).save(any(Transaction.class));
    }
}
