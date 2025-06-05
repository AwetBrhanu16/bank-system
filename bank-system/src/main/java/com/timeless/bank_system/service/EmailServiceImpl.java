package com.timeless.bank_system.service;

import com.timeless.bank_system.dto.EmailDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {


    private final JavaMailSender javaMailSender;

    //@Value("${spring.mail.username}")
    private String emailSender = "awetbrhanu122119@gmail.com";

    @Override
    @Async
    public void sendEmailAlert(EmailDetails emailDetails) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(emailSender); // ✅ correct
            mailMessage.setTo(emailDetails.getRecipient());
            mailMessage.setSubject(emailDetails.getSubject());
            mailMessage.setText(emailDetails.getMessageBody());

            javaMailSender.send(mailMessage);
            System.out.println("Email sent successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
