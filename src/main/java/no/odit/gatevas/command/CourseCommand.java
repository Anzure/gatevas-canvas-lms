package no.odit.gatevas.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.cli.CommandListener;
import no.odit.gatevas.model.Subject;
import no.odit.gatevas.service.CourseService;

@Component
public class CourseCommand implements CommandHandler {

	private static final Logger log = LoggerFactory.getLogger(CourseCommand.class);

	@Autowired
	private CourseService courseService;
	
	@Autowired
	private CommandListener commandListener;

	public void handleCommand(Command cmd) {
		String[] args = cmd.getArgs();

		if (args.length != 1) {
			cmd.printError(log);
			return;
		}


		if (args[0].equalsIgnoreCase("list")) {


		}
		else if (args[0].equalsIgnoreCase("add")) {

			Subject course = new Subject();

			System.out.print("Enter course short name: ");
			course.setShortName(commandListener.getScanner().nextLine());

			System.out.print("Enter course long name: ");
			course.setShortName(commandListener.getScanner().nextLine());

			System.out.print("Enter google sheet id: ");
			course.setGoogleSheetId(commandListener.getScanner().nextLine());

			System.out.print("Enter communication link: ");
			course.setCommunicationLink(commandListener.getScanner().nextLine());

			System.out.print("Shall social group be used? (Y/N): ");
			if (commandListener.getScanner().nextLine().equalsIgnoreCase("Y")) {
				System.out.println("Enter social group link: ");
				course.setSocialGroup(commandListener.getScanner().nextLine());
			}

			log.info("Creating course...");
			log.debug(course.toString());
			course = courseService.addCourse(course);
			log.info("Course created with ID " + course.getId().toString() + ".");

		}
		else if (args[0].equalsIgnoreCase("remove")) {	


		}
		else if (args[0].equalsIgnoreCase("info")) {

			System.out.print("Enter course name: ");
			String courseName = commandListener.getScanner().nextLine();

			log.debug("Searching for course...");
			courseService.getCourse(courseName).ifPresentOrElse((course) -> {
				log.info("Course search result");
				log.info(course.toString());
			}, () -> {
				log.error("Could not find course '" + courseName + "'!");
			});

		}

	}
}