package com.timeless.bank_system.service;

import com.timeless.bank_system.dto.EmailDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmailAlert(EmailDetails emailDetails) {

        try {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
            //    @Value("{spring.mail.username}")
            String emailSender = "awetbrhanu122119@gmail.com";
            mailMessage.setFrom(emailSender);
        mailMessage.setTo(emailDetails.getRecipient());
        mailMessage.setSubject(emailDetails.getSubject());
        mailMessage.setText(emailDetails.getMessageBody());
        javaMailSender.send(mailMessage);
            System.out.println("Email sent successfully");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
