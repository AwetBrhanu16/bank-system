package com.timeless.bank_system.service;

import com.timeless.bank_system.dto.TransactionDto;
import com.timeless.bank_system.entity.Transaction;
import com.timeless.bank_system.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public void saveTransaction(TransactionDto transactionDto) {

        Transaction transaction = Transaction
                .builder()
                .transactionId(transactionDto.getTransactionId())
                .accountNumber(transactionDto.getAccountNumber())
                .transactionType(transactionDto.getTransactionType())
                .amount(transactionDto.getAmount())
                .status("Success")
                .build();
        transactionRepository.save(transaction);
    }
}
