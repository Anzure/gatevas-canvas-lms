package no.odit.gatevas.command;

import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;

@Component
public class ExitCommand implements CommandHandler {

	@Autowired
	private Scanner commandScanner;

	public void handleCommand(Command command) {
		System.out.println("Shutting down from CLI.");
		commandScanner.close();
	}
}
