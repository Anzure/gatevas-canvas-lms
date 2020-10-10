package no.odit.gatevas.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

	@Autowired
	private JavaMailSender emailSender;
	
	@Value("${mail.smtp.email}")
	private String email;

	public void sendSimpleMessage(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage(); 
		message.setFrom(email);
		message.setTo(to); 
		message.setSubject(subject); 
		message.setText(text);
		emailSender.send(message);
	}
}