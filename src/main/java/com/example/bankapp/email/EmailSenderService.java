package com.example.bankapp.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


@Component
public class EmailSenderService {
	 @Autowired
	 private JavaMailSender mailSender;

	    public void sendSimpleEmail(String to, String subject, String body) {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setFrom("mohannaik050@gmail.com");
	        message.setTo(to);
	        message.setSubject(subject);
	        message.setText(body);

	        mailSender.send(message);
	        System.out.println("mail send successfully");
	    }

}

