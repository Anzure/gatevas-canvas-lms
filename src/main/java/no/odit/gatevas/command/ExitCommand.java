package no.odit.gatevas.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.cli.CommandListener;

@Component
public class ExitCommand implements CommandHandler {

	private static final Logger log = LoggerFactory.getLogger(ExitCommand.class);

	@Autowired
	private CommandListener commandListener;

	public void handleCommand(Command command) {
		log.info("Shutting down from CLI.");
		commandListener.stop();
	}
}
