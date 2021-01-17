package no.odit.gatevas.command;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.StudentService;

@Component
public class CustomCommand implements CommandHandler {

	@Autowired
	private StudentService studentService;

	public void handleCommand(Command cmd) {

		try {

			File in = new File("C:\\Users\\andre\\Documents\\input.csv");
			FileWriter out = new FileWriter(new File("C:\\Users\\andre\\Documents\\output_1.csv"));

			out.write('\ufeff');

			CSVParser parser = CSVParser.parse(in, StandardCharsets.ISO_8859_1, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withFirstRecordAsHeader());

			String[] header = {"Tidsmerke", "E-postadresse", "Jeg melder meg på følgende del- utdanning", "Arbeidsstatus", "Bransjetilhørighet",
					"Etternavn", "Fornavn", "Adresse privat", "Postnr og sted (Skriv begge deler)",
					"Antall år i bransjen", "Tlf nr", "Fødselsdato ", "Status", "Kommentarer"};
			CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

			for (CSVRecord row : parser.getRecords()) {

				String time = row.get("Tidsmerke");
				String email = row.get("E-postadresse");
				String admission = row.get("Jeg melder meg på følgende del- utdanning");
				String workStatus = row.get("Arbeidsstatus");
				String branch = row.get("Bransjetilhørighet");
				String lastName = row.get("Etternavn");
				String firstName = row.get("Fornavn");
				String address = row.get("Adresse privat");
				String postal = row.get("Postnr og sted (Skriv begge deler)");
				String years = row.get("Antall år i bransjen");
				String phone = row.get("Tlf nr");
				String birth = row.get("Fødselsdato ");
				String comment = row.get("Kommentarer");

				String courseName = admission.split("–|\\,")[0].toLowerCase().replace(" ", "");

				Optional<Student> optStudent = studentService.getUserByEmail(email);
				if (optStudent.isEmpty()) optStudent = studentService.getUserByName(firstName, lastName);

				// Check if student exists
				if (optStudent.isPresent()) {
					Student student = optStudent.get();

					Set<RoomLink> enrollments = student.getEnrollments();
					Optional<RoomLink> enrollment = enrollments.stream().filter(match -> {
						String name = match.getCourse().getLongName().toLowerCase();
						name = name.replace(" ", "");
						name = name.replace("h20", "");
						name = name.replace("v20", "");
						name = name.replace("s20", "");
						name = name.replace("bransjeprogram", "");
						return courseName.startsWith(name);

					}).findFirst();

					// Already offered
					if (enrollment.isPresent()) {
						printer.printRecord(time, email, admission, workStatus, branch, lastName, firstName, address
								, postal, years, phone, birth, "Har fått tilbud", comment);
						continue;
					}
				}

				// On wait-list
				if (time != null && time.length() > 2) {
					printer.printRecord(time, email, admission, workStatus, branch, lastName, firstName, address
							, postal, years, phone, birth, "På venteliste", comment);
				}
			}

			parser.close();
			printer.close();
			out.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


}
