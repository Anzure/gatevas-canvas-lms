package no.odit.gatevas.misc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailSender {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${mail.smtp.email}")
    private String email;

    @Value("${mail.smtp.name}")
    private String name;

    @Value("${mail.smtp.contact.email}")
    private String contactEmail;

    @Value("${mail.smtp.contact.name}")
    private String contactName;

    public void sendSimpleMessage(String to, String subject, String text) {
        try {

            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(email, name);
            helper.setTo(to);
            helper.setBcc(contactEmail);
            helper.setReplyTo(contactEmail, contactName);
            helper.setSubject(subject);
            helper.setText(text, true);

            emailSender.send(mimeMessage);

        } catch (Exception ex) {
            log.warn("Failed to send email to " + to + ".", ex);
        }
    }

}