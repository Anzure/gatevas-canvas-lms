package no.odit.gatevas.misc;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.service.StudentService;

@Component
public class GoogleSheetIntegration {

	private static final Logger log = LoggerFactory.getLogger(GoogleSheetIntegration.class);

	@Autowired
	private StudentService studentService;

	@Autowired
	private Sheets sheetService;

	@Autowired
	private CourseService courseService;

	/**
	 * Import students from online Google Spreadsheet
	 * @param spreadSheetId Google Spreadsheets document identifier
	 * @return List of created and existing students
	 * @throws IOException Failed to connect to Google API
	 */
	public Set<Student> processSheet(String spreadSheetId) throws IOException {

		ValueRange response = sheetService.spreadsheets().values()
				.get(spreadSheetId, "A:Z")
				.execute();

		// Output list
		Set<Student> students = new HashSet<Student>();

		// Online sheet data
		List<List<Object>> values = response.getValues();

		// Scan and collect
		if (values == null || values.isEmpty()) {
			log.warn("No data found in spreadsheet.");
		} else {
			HashMap<String, Integer> header = new HashMap<String, Integer>();
			for (List<Object> rawRow : values) {
				List<String> row = Lists.transform(rawRow, Functions.toStringFunction());

				// Load header
				if (header.isEmpty()) {
					int i = 0;
					for (String raw : row) {
						raw = raw.toLowerCase();
						if ((raw.startsWith("e-postadresse") || raw.startsWith("mail") || raw.startsWith("e-post") || raw.startsWith("epost"))
								&& !raw.contains("faktura"))
							raw = "email";
						else if (raw.startsWith("tlf") || raw.startsWith("telefon") || raw.startsWith("mobil"))
							raw = "phone";
						else if (raw.startsWith("fornavn")) 
							raw = "first_name";
						else if (raw.startsWith("etternavn")) 
							raw = "last_name";
						else if (raw.startsWith("jeg melder meg p"))
							raw = "course_type";
						header.put(raw, i);
						i++;
					}

				}
				// Process rows
				else {
					String firstName = row.get(header.get("first_name"));
					String lastName = row.get(header.get("last_name"));
					String email = row.get(header.get("email"));
					String phoneInput = row.get(header.get("phone"));
					int phoneNum = 0;
					try {
						phoneNum = Integer.parseInt(phoneInput.replace("+47", "").replace(" ", ""));
					} catch (Exception ex) {}

					Student student = studentService.createStudent(email, firstName, lastName, phoneNum);
					students.add(student);

					if (header.containsKey("course_type")) {
						String typeName = row.get(header.get("course_type"));
						if (typeName.contains(",")) {
							typeName = typeName.split(",")[0];
							CourseType courseType = courseService.getCourseType(typeName).orElse(null);
							if (courseType == null) log.warn("Failed to find course type '" + typeName + "'.");
							courseService.createCourseApplication(student, courseType);
						} else
							log.warn("Failed to split '" + typeName + "' while importing spreadsheets.");
					}
				}
			}
		}

		// Return list
		return students;
	}
}