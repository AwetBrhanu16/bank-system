package com.timeless.bank_system.service;

import com.timeless.bank_system.dto.EmailDetails;

public interface EmailService {

    void sendEmailAlert(EmailDetails emailDetails);
}
