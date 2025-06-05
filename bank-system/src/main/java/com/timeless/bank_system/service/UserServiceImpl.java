package com.timeless.bank_system.service;

import com.timeless.bank_system.config.JwtTokenProvider;
import com.timeless.bank_system.dto.*;
import com.timeless.bank_system.entity.Role;
import com.timeless.bank_system.entity.User;
import com.timeless.bank_system.repository.UserRepository;
import com.timeless.bank_system.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TransactionService transactionService;


    @Override
    public BankResponse login(LoginDto loginDto) {

        Authentication authentication = null;
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(),loginDto.getPassword()));

        EmailDetails loginAlert = EmailDetails
                .builder()
                .subject("YOU ARE LOGGED IN ")
                .recipient(loginDto.getEmail())
                .messageBody("You logged in in to your account")
                .build();

        emailService.sendEmailAlert(loginAlert);

        return BankResponse.builder()
                .responseCode("LOGIN SUCCESSFULLY")
                .responseMessage(jwtTokenProvider.generateToken(loginDto.getEmail()))
                .build();

    }

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
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .role(Role.valueOf("ROLE_USER"))
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

        TransactionDto transactionDto = TransactionDto
                .builder()
                .accountNumber(userToCredit.getAccountNumber())
                .transactionId("CREDIT")
                .amount(creditDebitRequest.getAmount())
                .build();
        transactionService.saveTransaction(transactionDto);

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

        User sourceAccountUser = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

        int availableBalance = sourceAccountUser.getAccountBalance().intValue();
        int amountToDept = creditDebitRequest.getAmount().intValue();

        if (availableBalance < amountToDept) {
            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }else {
            sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(creditDebitRequest.getAmount()));
            userRepository.save(sourceAccountUser);

        }

        TransactionDto transactionDto = TransactionDto
                .builder()
                .accountNumber(sourceAccountUser.getAccountNumber())
                .transactionId("Debit")
                .amount(creditDebitRequest.getAmount())
                .build();
        transactionService.saveTransaction(transactionDto);

        return BankResponse
                .builder()
                .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_DEBITED_MESSAGE)
                .accountInfo(
                        AccountInfo
                                .builder()
                                .accountNumber(AccountUtils.generateAccountNumber())
                                .accountName(sourceAccountUser.getFirstName() + " " + sourceAccountUser.getLastName() + " " + sourceAccountUser.getOtherName())
                                .accountBalance(sourceAccountUser.getAccountBalance())
                                .build()
                )
                .build();

    }

    @Override
    @Transactional
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


        User sourceAccountUser = userRepository.findByAccountNumber(transferRequest.getSourceAccount());
        String sourceAccountUserName = sourceAccountUser.getFirstName() + " " + sourceAccountUser.getLastName() + " " + sourceAccountUser.getOtherName();

        if (transferRequest.getAmount().compareTo(sourceAccountUser.getAccountBalance().doubleValue()) > 0) {
            return BankResponse
                    .builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(BigDecimal.valueOf(transferRequest.getAmount())));
        userRepository.save(sourceAccountUser);

        EmailDetails debtAlert = EmailDetails
                .builder()
                .subject("DEPT ALERT")
                .recipient(sourceAccountUser.getEmail())
                .messageBody(transferRequest.getAmount() + " birr has been deducted from your account! your account balance is " + sourceAccountUser.getAccountBalance())
                .build();
        emailService.sendEmailAlert(debtAlert);


        User destinationAccountUser = userRepository.findByAccountNumber(transferRequest.getDestinationAccount());
        //String destinationAccountUserName = sourceAccountUser.getFirstName() + " " + sourceAccountUser.getLastName() + " " + sourceAccountUser.getOtherName();
        destinationAccountUser.setAccountBalance(destinationAccountUser.getAccountBalance().add(BigDecimal.valueOf(transferRequest.getAmount())));

        userRepository.save(destinationAccountUser);
        EmailDetails creditAlert = EmailDetails
                .builder()
                .subject("CREDIT ALERT")
                .recipient(sourceAccountUser.getEmail())
                .messageBody(transferRequest.getAmount() + " birr has been sent to your account! from " + sourceAccountUserName + " your account balance is " + destinationAccountUser.getAccountBalance())
                .build();
        emailService.sendEmailAlert(creditAlert);

        TransactionDto transactionDto = TransactionDto
                .builder()
                .accountNumber(destinationAccountUser.getAccountNumber())
                .transactionType("CREDIT")
                .amount(BigDecimal.valueOf(transferRequest.getAmount()))
                .build();
        transactionService.saveTransaction(transactionDto);


        return BankResponse
                .builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESSFUL_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE)
                .accountInfo(null)
                .build();
    }


}
