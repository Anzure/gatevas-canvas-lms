package no.odit.gatevas.command;

import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.dao.CourseApplicationRepo;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.service.EnrollmentService;
import no.odit.gatevas.service.StudentService;
import no.odit.gatevas.type.ApplicationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class StudentCommand implements CommandHandler {

	@Autowired
	private CourseService courseService;

	@Autowired
	private Scanner commandScanner;

	@Autowired
	private StudentService studentService;

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private CourseApplicationRepo courseApplicationRepo;

	@Override
	public void handleCommand(Command cmd) {
		String[] args = cmd.getArgs();

		if (args.length != 1) {
			System.out.println("Available commands:");
			System.out.println("- student leave");
			System.out.println("- student join");
			System.out.println("- student status");
			return;
		}

		if (args[0].equalsIgnoreCase("leave")) {

			System.out.println("Remove student from course.");

			System.out.print("Student email: ");
			String identifier = commandScanner.nextLine();
			studentService.getUserByEmail(identifier).ifPresentOrElse(student -> {

				System.out.print("Enter course type: ");
				String courseName = commandScanner.nextLine();
				courseService.getCourseType(courseName).ifPresentOrElse((courseType) -> {

					// Enrollment in active course
					student.getEnrollments().stream().filter(enroll -> enroll.getCourse().getType().equals(courseType))
					.findFirst().ifPresentOrElse(enrollment -> {
						enrollment.setEmailSent(true);
						enrollment.setTextSent(true);
						enrollmentService.saveChanges(enrollment);
						System.out.println("Updated enrollment in '" + enrollment.getCourse().getShortName() + "' for '"
								+ student.getFirstName() + " " + student.getLastName() + "'.");

					}, () -> {
						System.out.println("Could not find active enrollment, ignoring that.");
					});

					// Course application
					courseApplicationRepo.findByStudentAndCourse(student, courseType).ifPresentOrElse(apply -> {
						apply.setStatus(ApplicationStatus.WITHDRAWN);
						courseApplicationRepo.saveAndFlush(apply);
						System.out.println("Updated course applicationn status for '" + student.getFirstName() + " " + student.getLastName()
						+ "' in '" + courseType.getShortName() + "'.");

					}, () -> {
						System.out.println("Could not find course application for student!");
					});

				}, () -> {
					System.out.println("Could not find course '" + courseName + "'!");
				});

			}, () -> {
				System.out.println("Failed to find student!");
			});
		}

		else if (args[0].equalsIgnoreCase("join")) {

			System.out.println("Add student from course.");

			System.out.print("Student email: ");
			String identifier = commandScanner.nextLine();
			studentService.getUserByEmail(identifier).ifPresentOrElse(student -> {

				System.out.print("Enter course type: ");
				String courseName = commandScanner.nextLine();
				courseService.getCourseType(courseName).ifPresentOrElse((courseType) -> {

					// Course application
					courseApplicationRepo.findByStudentAndCourse(student, courseType).ifPresentOrElse(apply -> {
						apply.setStatus(ApplicationStatus.ACCEPTED);
						courseApplicationRepo.saveAndFlush(apply);
						System.out.println("Updated course applicationn status for '" + student.getFirstName() + " " + student.getLastName()
						+ "' in '" + courseType.getShortName() + "'.");

					}, () -> {
						System.out.println("Could not find course application for student!");
					});

				}, () -> {
					System.out.println("Could not find course '" + courseName + "'!");
				});

			}, () -> {
				System.out.println("Failed to find student!");
			});
		}
	}
}
