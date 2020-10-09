package no.odit.gatevas.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.cli.CommandListener;

public class CourseCommand extends CommandListener implements CommandHandler {

	private static final Logger log = LoggerFactory.getLogger(CourseCommand.class);

	public void handleCommand(Command cmd) {
		String[] args = cmd.getArgs();
		
		if (args.length <= 0) {
			cmd.printError(log);
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("list")) {
			
			
		}
		else if (args[0].equalsIgnoreCase("add")) {
			
			System.out.print("Enter course name: ");
			String courseName = super.scanner.nextLine();
			
			System.out.print("Enter course ID: ");
			String courseId = super.scanner.nextLine();
			
			log.info("Created course " + courseName + " ID " + courseId + ".");
			
		}
		else if (args[0].equalsIgnoreCase("remove")) {	
			
			
		}
		else if (args[0].equalsIgnoreCase("info")) {
			
			
		}
		
	}
}