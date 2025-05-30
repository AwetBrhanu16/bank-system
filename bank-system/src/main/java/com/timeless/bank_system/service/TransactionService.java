package com.timeless.bank_system.service;

import com.timeless.bank_system.dto.TransactionDto;
import org.springframework.stereotype.Component;

@Component
public interface TransactionService {
    void saveTransaction(TransactionDto transactionDto);
}
