package com.example.bankapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.example.bankapp.email.EmailSenderService;

@SpringBootApplication
public class BankappApplication {
    
    @Autowired
    private EmailSenderService emailsenderservice;

    public static void main(String[] args) {
        SpringApplication.run(BankappApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void sendMailOnStartup() {
        String to = "elmhurstbank@gmail.com";
        String subject = "Application Started";
        String body = "Hello,\n\nThe Spring Boot application has started successfully.\n\nCheers!";

        emailsenderservice.sendSimpleEmail(to, subject, body);
        
    }
}