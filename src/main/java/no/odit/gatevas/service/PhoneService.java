package no.odit.gatevas.service;

import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import no.odit.gatevas.dao.PhoneRepo;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.Phone;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;

@Component
public class PhoneService {

	private static final Logger log = LoggerFactory.getLogger(PhoneService.class);

	@Value("${gatevas.sms.sender}")
	private String sender;

	@Value("${gatevas.sms.token}")
	private String token;

	@Value("${gatevas.sms.country}")
	private int country;

	@Autowired
	private PhoneRepo phoneRepo;

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private CanvasService canvasService;

	public Phone createPhone(int phoneNumber) {

		if (phoneNumber == 0) return null;

		// TODO: Return existing phone


		// Create new phone
		Phone phone = new Phone();
		phone.setPhoneNumber(phoneNumber);
		phone.setCountryCode(country);
		phone = phoneRepo.saveAndFlush(phone);
		log.debug("CREATE PHONE -> " + phone.toString());
		return phone;
	}

	public boolean sendSMS(Classroom classRoom) {

		canvasService.syncUsersReadOnly(classRoom);

		for (RoomLink enrollment : classRoom.getEnrollments()) {

			if (enrollment.isTextSent()) {
				continue;
			}

			Student student = enrollment.getStudent();
			if (student.getCanvasStatus() != CanvasStatus.EXISTS) {
				continue;
			}

			if (enrollment.getCanvasStatus() != CanvasStatus.EXISTS) {
				continue;
			}

			enrollment.setTextSent(true);
			enrollmentService.saveChanges(enrollment);

			sendSMS(classRoom, student, false);

		}
		return true;
	}

	public boolean sendSMS(Classroom classRoom, Student student, boolean isTest) {
		try {

			StringBuilder msg = new StringBuilder();
			msg.append(classRoom.getLongName() + "\nBrukernavn: " + student.getEmail());
			if (student.isExportedToCSV()) msg.append("\nPassord: " + student.getTmpPassword());
			msg.append("\nSe epost for mer info.\n(sjekk eventuelt spam-mappen)");

			String txt = msg.toString();

			Phone phone = student.getPhone();
			int phoneNumber = isTest ? 45660785 : phone.getPhoneNumber();

			URL url = new URL("https://gatewayapi.com/rest/mtsms");
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoOutput(true);

			if (sender.length() > 10) sender = sender.substring(0, 11);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(
					"token=" + token
					+ "&sender=" + URLEncoder.encode(sender, "UTF-8")
					+ "&message=" + URLEncoder.encode(txt, "UTF-8")
					+ "&class=premium&priority=VERY_URGENT&recipients.0.msisdn=" + phoneNumber
					);
			wr.close();

			int responseCode = con.getResponseCode();
			log.debug("SMS sent to " + phoneNumber + ", response code:" + responseCode);
			return responseCode == 200 ? true : false;

		} catch (Exception ex) {
			log.warn("Failed to send SMS.", ex);
		}
		return false;
	}

}