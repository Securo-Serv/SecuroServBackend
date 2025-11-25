package com.example.SecuroServBackend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOtp(String toEmail, String otp) {
        System.out.println("Sending OTP to " + toEmail);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("securoservpvtltd@gmail.com");
        message.setSubject("Your SecuroServ OTP for Sign Up");
        message.setText("Your OTP is: " + otp);
        System.out.println("OTP send to " + toEmail);
        javaMailSender.send(message);
    }
}
