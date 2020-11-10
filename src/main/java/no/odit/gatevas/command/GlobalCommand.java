package no.odit.gatevas.command;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.dao.CourseApplicationRepo;
import no.odit.gatevas.misc.GoogleSheetIntegration;
import no.odit.gatevas.model.CourseApplication;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.Phone;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.CanvasService;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.service.EmailService;
import no.odit.gatevas.service.EnrollmentService;
import no.odit.gatevas.service.PhoneService;
import no.odit.gatevas.service.StudentService;
import no.odit.gatevas.type.ApplicationStatus;

@Component
public class GlobalCommand implements CommandHandler {

	@Autowired
	private CourseService courseService;

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private Scanner commandScanner;

	@Autowired
	private StudentService studentService;

	@Autowired
	private CanvasService canvasService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PhoneService phoneService;

	@Autowired
	private GoogleSheetIntegration googleSheetIntegration;

	@Autowired
	private CourseApplicationRepo courseApplicationRepo;

	public void handleCommand(Command cmd) {
		String[] args = cmd.getArgs();

		if (args.length != 1) {
			System.out.println("Available commands:");
			System.out.println("- global import");
			return;
		}

		// Import students from global Google Sheets
		if (args[0].equalsIgnoreCase("import")) {

			System.out.println("Import students to system.");

			Set<String> globalSheets = new HashSet<String>();
			for (CourseType type : courseService.getCourseTypes()) {
				globalSheets.add(type.getGoogleSheetId());
			}

			System.out.println("Found " + globalSheets.size() + " sheets to process.");
			System.out.print("Want to continue? (Y/N): ");
			if (commandScanner.nextLine().equalsIgnoreCase("Y")) {
				globalSheets.forEach(googleSpreadSheetId -> {
					try {
						Set<Student> students = googleSheetIntegration.processSheet(googleSpreadSheetId);
						System.out.println("Successfully processed " + students.size() + " students.");

					} catch (Exception ex) {
						ex.printStackTrace();
						System.out.println("Failed to process sheet!");
					}
				});
			}
		}

		// Export students to csv file
		else if (args[0].equalsIgnoreCase("export")) {

			System.out.println("Export students to file.");

			System.out.print("Status (all/waitlist): ");
			String statusInput = commandScanner.nextLine().toUpperCase();
			ApplicationStatus status = null;
			if (!statusInput.equalsIgnoreCase("all")) status = ApplicationStatus.valueOf(statusInput);

			List<CourseApplication> applications = null;
			if (status == null) applications = courseApplicationRepo.findAll();
			else applications = courseApplicationRepo.findByStatus(status);

			System.out.print("Output file path: ");
			try (FileWriter out = new FileWriter(new File(commandScanner.nextLine()))){
				out.write('\ufeff');
				
				String[] header = {"E-postadresse", "Kurs", "Fornavn", "Etternavn", "Tlf nr", "Status"};
				CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

				for (CourseApplication apply : applications) {
					Student student = apply.getStudent();
					Phone phone = student.getPhone();
					printer.printRecord(student.getEmail(), apply.getCourse().getLongName(),
							student.getFirstName(), student.getLastName(), phone.getPhoneNumber(), apply.getStatus().toString());
				}
				
				printer.close();

			} catch (Exception ex) {
				System.out.println("Failed to export global list.");
			}
		}

	}
}