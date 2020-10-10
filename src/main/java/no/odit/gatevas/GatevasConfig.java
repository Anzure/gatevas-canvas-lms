package no.odit.gatevas;

import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.command.CourseCommand;
import no.odit.gatevas.command.ExitCommand;
import no.odit.gatevas.command.StudentCommand;
import no.odit.gatevas.command.TestCommand;

@Configuration
public class GatevasConfig {

	@Value("${mail.smtp.port}")
	private int port;

	@Value("${mail.smtp.host}")
	private String host;

	@Value("${mail.smtp.username}")
	private String username;

	@Value("${mail.smtp.password}")
	private String password;

	@Value("${mail.smtp.name}")
	private String name;

	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(host);
		mailSender.setPort(port);

		mailSender.setUsername(username);
		mailSender.setPassword(password);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "true");

		return mailSender;
	}

	@Autowired
	private CourseCommand courseCommand;

	@Autowired
	private ExitCommand exitCommand;

	@Autowired
	private StudentCommand studentCommand;
	
	@Autowired
	private TestCommand testCommand;

	@Bean
	public List<CommandHandler> getCommands() {
		return List.of(courseCommand, exitCommand, studentCommand, testCommand);
	}

	@Bean
	public Scanner getCommandScanner() {
		return new Scanner(System.in);
	}
}