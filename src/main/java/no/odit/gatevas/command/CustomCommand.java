package no.odit.gatevas.command;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.ksu.canvas.model.Enrollment;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.StudentService;

@Component
public class CustomCommand implements CommandHandler {

	@Autowired
	private Scanner commandScanner;

	@Autowired
	private StudentService studentService;

	public void handleCommand(Command cmd) {
		//		String[] args = cmd.getArgs();
		//
		//		if (args.length != 1) {
		//			System.out.println("Mising argument!");
		//			return;
		//		}
		//
		//		if (args[0].equalsIgnoreCase("")) {
		//			
		//		}

		try {

			FileReader in = new FileReader(new File(""));
			FileWriter out = new FileWriter(new File(""));

			CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());

			Map<String, Integer> headerMap = parser.getHeaderMap();
			String[] headerList = headerMap.keySet().toArray(new String[0]);

			CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headerList));

			parser.forEach(row -> {

				String email = row.get("E-postadresse");
				String firstName = row.get("Fornavn");
				String lastName = row.get("Etternavn");
				String admission = row.get("Jeg melder meg på følgende del- utdanning");
				String courseName = admission.split("–|\\.")[0].toLowerCase().replace(" ", "");

				studentService.getUserByName(firstName, lastName).ifPresentOrElse(student -> {
					
					
					Set<RoomLink> enrollments = student.getEnrollments();
					Optional<RoomLink> enrollment = enrollments.stream().filter(match -> {
						String name = match.getCourse().getLongName().toLowerCase();
						name = name.replace(" ", "");
						name = name.replace("h20", "");
						name = name.replace("bransjeprogram", "");
						return courseName.startsWith(name);
						
						
					}).findFirst();

					if (enrollment.isPresent()) {
						
					}
					else {

					}


				}, () -> {
					printer.printRecords(row);
				});

			});


			parser.close();
			printer.close();
			in.close();
			out.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
