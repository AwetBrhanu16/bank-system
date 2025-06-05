package com.timeless.bank_system.service;

import com.timeless.bank_system.dto.TransactionDto;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {
    void saveTransaction(TransactionDto transactionDto);
}
