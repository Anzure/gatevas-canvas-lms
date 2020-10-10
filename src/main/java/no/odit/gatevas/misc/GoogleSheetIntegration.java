package no.odit.gatevas.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.StudentService;

@Component
public class GoogleSheetIntegration {

	private static final Logger log = LoggerFactory.getLogger(GoogleSheetIntegration.class);

	@Autowired
	private StudentService studentService;

	@Autowired
	private Sheets sheetService;

	public List<Student> processSheet(String courseId, String spreadSheetId) throws Exception {

		ValueRange response = sheetService.spreadsheets().values()
				.get(spreadSheetId, "A:Z")
				.execute();

		// Output list
		List<Student> students = new ArrayList<Student>();

		// Online sheet data
		List<List<Object>> values = response.getValues();

		// Scan and collect
		if (values == null || values.isEmpty()) {
			System.out.println("No data found.");
		} else {
			HashMap<String, Integer> header = new HashMap<String, Integer>(); 
			for (List<Object> rawRow : values) {
				List<String> row = Lists.transform(rawRow, Functions.toStringFunction());

				// Load header
				if (header.isEmpty()) {
					int i = 0;
					for (String raw : row) {
						if (raw.toLowerCase().startsWith("mail") || raw.toLowerCase().startsWith("e-post") || raw.toLowerCase().startsWith("epost"))
							raw = "E-postadresse";
						else if (raw.toLowerCase().startsWith("tlf") || raw.toLowerCase().startsWith("telefon") || raw.toLowerCase().startsWith("mobil"))
							raw = "Tlf nr";
						header.put(raw, i);
						i++;
					}

				}
				// Process rows
				else {
					String firstName = row.get(header.get("Fornavn"));
					String lastName = row.get(header.get("Etternavn"));
					String email = row.get(header.get("E-postadresse"));
					int phoneNum = Integer.parseInt((row.get(header.get("Tlf nr"))).replace("+47", "").replace(" ", ""));

					Student student = studentService.createStudent(email, firstName, lastName, phoneNum);
					log.debug(student.toString());
					students.add(student);
				}
			}
		}

		// Return list
		return students;
	}
}