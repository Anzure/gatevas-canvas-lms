package no.odit.gatevas.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.cli.CommandListener;

public class StudentCommand extends CommandListener implements CommandHandler {
	
	private static final Logger log = LoggerFactory.getLogger(StudentCommand.class);

	public void handleCommand(Command cmd) {
		String[] args = cmd.getArgs();
		
		if (args.length != 1) {
			cmd.printError(log);
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("import")) {
			
		}
		else if (args[0].equalsIgnoreCase("import")) {
			
		}
	}
}