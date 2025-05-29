package com.timeless.bank_system.service;

import com.timeless.bank_system.dto.*;
import com.timeless.bank_system.entity.User;
import com.timeless.bank_system.repository.UserRepository;
import com.timeless.bank_system.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public BankResponse createAccount(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {

            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();

        }
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(newUser);

        EmailDetails emailDetails = EmailDetails
                .builder()
                .subject("Account Created Successfully")
                .messageBody("Your account has been created successfully. Your account number is " + savedUser.getAccountNumber())
                .recipient(userRequest.getEmail())
                .build();

        emailService.sendEmailAlert(emailDetails);

       return BankResponse
                .builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accountInfo(null)
                .accountInfo(
                        AccountInfo
                                .builder()
                                .accountName(savedUser.getFirstName() + " " + savedUser.getLastName() + " " + savedUser.getOtherName())
                                .accountNumber(AccountUtils.generateAccountNumber())
                                .accountBalance(savedUser.getAccountBalance())
                                .build()
                )
                .build();
    }


    @Override
    public BankResponse balanceEnquiry(EnquiryRequest enquiryRequest) {

        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User user = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());

        return BankResponse
                .builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_SUCCESS)
                .accountInfo(AccountInfo
                        .builder()
                        .accountBalance(user.getAccountBalance())
                        .accountName(user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName())
                        .accountNumber(enquiryRequest.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest) {

        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if (!isAccountExist) {
            return AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE;
        }
        User user = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest creditDebitRequest) {

        boolean isAccountExist = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User userToCredit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(creditDebitRequest.getAmount()));
        userRepository.save(userToCredit);

        return BankResponse
                .builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo
                        .builder()
                        .accountName(userToCredit.getFirstName() + " " + userToCredit.getLastName() + " " + userToCredit.getOtherName())
                        .accountNumber(AccountUtils.generateAccountNumber())
                        .accountBalance(userToCredit.getAccountBalance())
                        .build())
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest creditDebitRequest) {

        boolean isAccountExist = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User userToDebit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

        int availableBalance = userToDebit.getAccountBalance().intValue();
        int amountToDept = creditDebitRequest.getAmount().intValue();

        if (availableBalance < amountToDept) {
            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }else {
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(creditDebitRequest.getAmount()));
            userRepository.save(userToDebit);

        }

        return BankResponse
                .builder()
                .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_DEBITED_MESSAGE)
                .accountInfo(
                        AccountInfo
                                .builder()
                                .accountNumber(AccountUtils.generateAccountNumber())
                                .accountName(userToDebit.getFirstName() + " " + userToDebit.getLastName() + " " + userToDebit.getOtherName())
                                .accountBalance(userToDebit.getAccountBalance())
                                .build()
                )
                .build();

    }

    @Override
    public BankResponse transfer(TransferRequest transferRequest) {

        boolean isSourceAccountExist = userRepository.existsByAccountNumber(transferRequest.getSourceAccount());
        if (!isSourceAccountExist) {
            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        boolean isDestinationAccountExist = userRepository.existsByAccountNumber(transferRequest.getSourceAccount());
        if (!isDestinationAccountExist) {
            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }


        User userFromTransfer= userRepository.findByAccountNumber(transferRequest.getSourceAccount());
        User userToTransfer = userRepository.findByAccountNumber(transferRequest.getDestinationAccount());

        userFromTransfer.setAccountBalance(userFromTransfer.getAccountBalance().subtract(BigDecimal.valueOf(transferRequest.getAmount())));
        userRepository.save(userFromTransfer);

        EmailDetails emailDetails = EmailDetails
                .builder()
                .subject("DEBT ALERT")
                .recipient(userFromTransfer.getEmail())
                .messageBody(transferRequest.getAmount() + " birr has been deducted from your account! your account balance is " + userFromTransfer.getAccountBalance())
                .build();

        emailService.sendEmailAlert(emailDetails);


        userToTransfer.setAccountBalance(userToTransfer.getAccountBalance().add(BigDecimal.valueOf(transferRequest.getAmount())));
        userRepository.save(userToTransfer);

        EmailDetails emailDetails1 = EmailDetails
                .builder()
                .subject("CREDIT ALERT")
                .recipient(userToTransfer.getEmail())
                .messageBody(transferRequest.getAmount() + " birr has been credited to your account! your account balance is " + userToTransfer.getAccountBalance())
                .build();

        emailService.sendEmailAlert(emailDetails1);


        return BankResponse
                .builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESSFUL_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE)
                .accountInfo(null)
                .build();
    }

}
