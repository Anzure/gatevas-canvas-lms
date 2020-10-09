package no.odit.gatevas.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.odit.gatevas.type.Command;
import no.odit.gatevas.type.CommandHandler;
import no.odit.gatevas.type.CommandListener;

public class CourseCommand extends CommandListener implements CommandHandler {

	private static final Logger log = LoggerFactory.getLogger(CourseCommand.class);

	public void handleCommand(Command command) {
		System.out.println("Test=" + command.getCmd());
	}
}