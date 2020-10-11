package no.odit.gatevas.command;

import org.springframework.stereotype.Component;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;

@Component
public class StudentCommand implements CommandHandler {

	public void handleCommand(Command cmd) {
		String[] args = cmd.getArgs();

		if (args.length != 1) {
			System.out.println("Available commands:");
			System.out.println("- ");
			return;
		}


		if (args[0].equalsIgnoreCase("import")) {

		}
		else if (args[0].equalsIgnoreCase("import")) {

		}
	}
}