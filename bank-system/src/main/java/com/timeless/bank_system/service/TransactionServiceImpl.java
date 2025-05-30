package com.timeless.bank_system.service;

import com.timeless.bank_system.dto.TransactionDto;
import com.timeless.bank_system.entity.Transaction;
import com.timeless.bank_system.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public void saveTransaction(TransactionDto transactionDto) {

        Transaction transaction = Transaction
                .builder()
                .transactionId(transactionDto.getTransactionId())
                .transactionType(transactionDto.getTransactionType())
                .amount(transactionDto.getAmount())
                .status("Success")
                .build();
        transactionRepository.save(transaction);
    }
}
