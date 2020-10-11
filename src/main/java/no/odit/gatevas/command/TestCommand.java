package no.odit.gatevas.command;

import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.misc.EmailSender;
import no.odit.gatevas.service.PhoneService;

@Component
public class TestCommand implements CommandHandler {

	@Autowired
	private Scanner commandScanner;

	@Autowired
	private EmailSender emailSender;

	@Autowired
	private PhoneService smsSender;

	@Override
	public void handleCommand(Command cmd) {
		String[] args = cmd.getArgs();

		if (args.length != 1) {
			System.out.println("Available commands:");
			System.out.println("- test mail");
			System.out.println("- test sms");
		}

		if (args[0].equalsIgnoreCase("mail")) {

			System.out.print("Email: ");
			String to = commandScanner.nextLine();

			System.out.print("Title: ");
			String title = commandScanner.nextLine();

			System.out.print("Message: ");
			String msg = commandScanner.nextLine();

			emailSender.sendSimpleMessage(to, title, msg);
			System.out.println("Sent email!");
		}
		else if (args[0].equalsIgnoreCase("sms")) {

			System.out.print("Phone: ");
			String to = commandScanner.nextLine();

			System.out.print("Text: ");
			String msg = commandScanner.nextLine();

			smsSender.sendSMS(msg, Integer.parseInt(to));
			System.out.println("Sent SMS!");
		}
	}	
}