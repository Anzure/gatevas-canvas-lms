package no.odit.gatevas.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.StudentService;

@Component
public class GoogleSheetIntegration {

	@Autowired
	private StudentService studentService;

	private final String APPLICATION_NAME = "Fagskolen Ekom";
	private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private final String CREDENTIALS_FOLDER = "credentials"; // Directory to store user credentials.
	private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
	private final String CLIENT_SECRET_DIR = "client_secret.json";

	private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = GoogleSheetIntegration.class.getResourceAsStream(CLIENT_SECRET_DIR);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
				.setAccessType("offline")
				.build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	public List<Student> processSheet(String courseId, String spreadSheetId) throws Exception {

		System.out.println("DEBUG...");

		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		final String range = "A:Z";
		Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();
		ValueRange response = service.spreadsheets().values()
				.get(spreadSheetId, range)
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
					System.out.println(student.toString()); //TODO
					students.add(student);
				}
			}
		}

		// Return list
		return students;
	}
}