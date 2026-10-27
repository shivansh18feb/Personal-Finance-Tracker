package com.financetracker.pro.service;

import com.financetracker.pro.document.Transaction;
import com.financetracker.pro.document.TransactionType;
import com.financetracker.pro.dto.TransactionRequest;
import com.financetracker.pro.dto.TransactionResponse;
import com.financetracker.pro.repository.TransactionRepository;
import com.financetracker.pro.repository.UserRepository;
import com.financetracker.pro.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = Transaction.builder()
                .id("tx-123")
                .userId("user-1")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("45.50"))
                .currency("USD")
                .category("Food")
                .description("Dinner at restaurant")
                .transactionDate(LocalDate.now())
                .deleted(false)
                .build();
    }

    @Test
    void testCreateTransaction() {
        TransactionRequest request = TransactionRequest.builder()
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("45.50"))
                .currency("USD")
                .category("Food")
                .description("Dinner at restaurant")
                .transactionDate(LocalDate.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        TransactionResponse response = transactionService.createTransaction("user-1", request);

        assertNotNull(response);
        assertEquals("tx-123", response.getId());
        assertEquals("user-1", response.getUserId());
        assertEquals(new BigDecimal("45.50"), response.getAmount());
        assertEquals("Food", response.getCategory());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionById_Success() {
        when(transactionRepository.findByIdAndUserIdAndDeletedFalse("tx-123", "user-1"))
                .thenReturn(Optional.of(sampleTransaction));

        TransactionResponse response = transactionService.getTransactionById("user-1", "tx-123");

        assertNotNull(response);
        assertEquals("tx-123", response.getId());
    }

    @Test
    void testDeleteTransaction_SoftDelete() {
        when(transactionRepository.findByIdAndUserIdAndDeletedFalse("tx-123", "user-1"))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        transactionService.deleteTransaction("user-1", "tx-123");

        assertTrue(sampleTransaction.isDeleted());
        verify(transactionRepository, times(1)).save(sampleTransaction);
    }
}
