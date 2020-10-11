package no.odit.gatevas.command;

import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.service.CourseService;

@Component
public class CourseCommand implements CommandHandler {

	@Autowired
	private CourseService courseService;

	@Autowired
	private Scanner commandScanner;

	public void handleCommand(Command cmd) {
		String[] args = cmd.getArgs();

		if (args.length != 1) {
			System.out.println("Available commands:");
			System.out.println("- course list");
			System.out.println("- course add");
			System.out.println("- course remove");
			System.out.println("- course info");
			System.out.println("- course import");
			return;
		}

		// View list of courses
		if (args[0].equalsIgnoreCase("list")) {

			List<Classroom> courses = courseService.getAllCourses();
			System.out.println("Course list (" + courses.size() + "):");
			courses.forEach(course -> System.out.println(course.toString()));

		}
		// Add a new course
		else if (args[0].equalsIgnoreCase("add")) {

			System.out.println("Create a new course.");
			Classroom course = new Classroom();

			System.out.print("Enter course short name: ");
			course.setShortName(commandScanner.nextLine());

			System.out.print("Enter course long name: ");
			course.setLongName(commandScanner.nextLine());

			System.out.print("Enter google sheet id: ");
			course.setGoogleSheetId(commandScanner.nextLine());

			System.out.print("Enter communication link: ");
			course.setCommunicationLink(commandScanner.nextLine());

			System.out.print("Shall social group be used? (Y/N): ");
			if (commandScanner.nextLine().equalsIgnoreCase("Y")) {
				System.out.println("Enter social group link: ");
				course.setSocialGroup(commandScanner.nextLine());
			}

			System.out.println("Creating course '" + course.getShortName() + "'...");
			course = courseService.addCourse(course);
			System.out.println("Course created with ID " + course.getId().toString() + ".");

		}
		// Remove a course
		else if (args[0].equalsIgnoreCase("remove")) {

			System.out.println("Remove an existing course.");
			System.out.print("Enter course name: ");
			String courseName = commandScanner.nextLine();

			courseService.getCourse(courseName).ifPresentOrElse((course) -> {
				System.out.println("Deleting '" + course.getShortName() + "'...");
				courseService.removeCourse(course);
				System.out.println("Deleted '" + courseName + "'.");
			}, () -> {
				System.out.println("Could not find course '" + courseName + "'!");
			});

		}
		// Get information about course
		else if (args[0].equalsIgnoreCase("info")) {

			System.out.println("Retrieve course details.");
			System.out.print("Enter course name: ");
			String courseName = commandScanner.nextLine();

			courseService.getCourse(courseName).ifPresentOrElse((course) -> {
				System.out.println("Details about course '" + course.getShortName() + "':");
				System.out.println(course.toString());
			}, () -> {
				System.out.println("Could not find course '" + courseName + "'!");
			});

		}
		// Import students from Google Sheets
		else if (args[0].equalsIgnoreCase("import")) {

			System.out.println("Import students to course.");
			System.out.print("Enter course name: ");
			String courseName = commandScanner.nextLine();

			courseService.getCourse(courseName).ifPresentOrElse((course) -> {

				System.out.println("Importing students from Google Spreadsheets...");
				courseService.importStudents(course).ifPresentOrElse(students -> {
					System.out.println("Imported " + students.size() + " students to '" + course.getShortName() + "'.");
				}, () -> {
					System.out.println("Failed to import students to '" + course.getShortName() + "'.");
				});

			}, () -> {
				System.out.println("Could not find course '" + courseName + "'!");
			});

		}
	}	
}