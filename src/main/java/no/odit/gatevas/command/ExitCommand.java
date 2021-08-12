package no.odit.gatevas.command;

import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ExitCommand implements CommandHandler {

	@Autowired
	private Scanner commandScanner;

	public void handleCommand(Command command) {
		System.out.println("Shutting down from CLI.");
		commandScanner.close();
	}
}
