package no.odit.gatevas.service;

import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.dao.PhoneRepo;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.Phone;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLEncoder;

@Component
@Slf4j
public class PhoneService {

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

    // Creates a Phone and saves to storage
    public Phone createPhone(Integer phoneNumber) {

        if (phoneNumber == null || phoneNumber == 0) return null;

        // Create new phone
        Phone phone = new Phone();
        phone.setPhoneNumber(phoneNumber);
        phone.setCountryCode(country);
        phone = phoneRepo.saveAndFlush(phone);
        log.debug("CREATE PHONE -> " + phone.toString());
        return phone;
    }

    // Sends SMS to students in course with enrollment details
    public boolean sendSMS(Classroom classRoom) {

        canvasService.syncUsersReadOnly(classRoom);

        for (RoomLink enrollment : classRoom.getEnrollments()) {
            if (enrollment.getTextSent()) {
                continue;
            }
            Student student = enrollment.getStudent();
            if (student.getCanvasStatus() != CanvasStatus.EXISTS) {
                continue;
            }
            if (enrollment.getCanvasStatus() != CanvasStatus.EXISTS
                    && enrollment.getCourse().getCanvasStatus() != CanvasStatus.IGNORE) {
                continue;
            }

            enrollment.setTextSent(true);
            enrollmentService.saveChanges(enrollment);
            sendSMS(classRoom, student, false);
        }
        return true;
    }

    // Sends SMS to student in course with enrollment details
    public boolean sendSMS(Classroom classRoom, Student student, boolean isTest) {
        try {

            StringBuilder msg = new StringBuilder();
            msg.append(classRoom.getLongName() + "\nBrukernavn: " + student.getEmail());
            if (student.getExportedToCSV()) msg.append("\nPassord: " + student.getTmpPassword());
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
                            + "&message=" + URLEncoder.encode(txt.replace("–", "-"), "UTF-8")
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