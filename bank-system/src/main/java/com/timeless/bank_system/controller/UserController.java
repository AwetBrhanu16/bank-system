package com.timeless.bank_system.controller;

import com.timeless.bank_system.dto.*;
import com.timeless.bank_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public BankResponse createAccount(@RequestBody UserRequest userRequest){
        return userService.createAccount(userRequest);
    }

    @PostMapping("/login")
    public BankResponse login(@RequestBody LoginDto loginDto){
        return userService.login(loginDto);
    }

    @GetMapping("/balanceEnquiry")
    public BankResponse balanceEnquiry(@RequestBody EnquiryRequest enquiryRequest){
        return userService.balanceEnquiry(enquiryRequest);
    }

    @GetMapping("/nameEnquiry")
    public String nameEnquiry(@RequestBody EnquiryRequest enquiryRequest){
        return userService.nameEnquiry(enquiryRequest);
    }

    @PostMapping("/creditAccount")
    public BankResponse creditAccount(@RequestBody CreditDebitRequest creditDebitRequest){
        return userService.creditAccount(creditDebitRequest);
    }

    @PostMapping("/debitAccount")
    public BankResponse debitAccount(@RequestBody CreditDebitRequest creditDebitRequest){
        return userService.debitAccount(creditDebitRequest);
    }

    @PostMapping("/transfer")
    public BankResponse transfer(@RequestBody TransferRequest transferRequest){
        return userService.transfer(transferRequest);
    }




}
