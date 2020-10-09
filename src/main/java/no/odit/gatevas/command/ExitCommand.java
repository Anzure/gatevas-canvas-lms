package no.odit.gatevas.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.cli.CommandListener;

public class ExitCommand extends CommandListener implements CommandHandler {

	private static final Logger log = LoggerFactory.getLogger(ExitCommand.class);

	public void handleCommand(Command command) {
		log.info("Shutting down from CLI.");
		super.stop();
	}
}
