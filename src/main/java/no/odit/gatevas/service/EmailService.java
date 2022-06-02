package no.odit.gatevas.service;

import no.odit.gatevas.misc.EmailSender;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private EmailSender emailSender;

    @Value("${mail.smtp.contact.email}")
    private String contactEmail;

    @Autowired
    private StudentService studentSerivce;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CanvasService canvasService;

    // Sends email to students in course with login and information.
    public void sendEmail(Classroom classRoom) {

        canvasService.syncUsersReadOnly(classRoom);

        for (RoomLink enrollment : classRoom.getEnrollments()) {

            if (enrollment.getEmailSent()) {
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

            enrollment.setEmailSent(true);
            enrollmentService.saveChanges(enrollment);
            sendEmail(classRoom, student, false);
        }
    }

    // Send email with login and information to student.
    public void sendEmail(Classroom classRoom, Student student, boolean isTest) {

        String email = isTest ? contactEmail : student.getEmail();
        String login = student.getLogin() != null && student.getLogin().equalsIgnoreCase(student.getEmail()) ? null : student.getLogin();
        login = isTest ? null : login;

        StringBuilder sb = new StringBuilder("<p>Hei</p>");

        sb.append("<b>Læringsplattform</b><br/>"
                + "Bruk følgende detaljer for å logge på Canvas.<br/>"
                + "Kobling: <a href=\"https://f-vt.instructure.com/login/canvas\">f-vt.instructure.com/login/canvas</a><br/>"
        );
        if (login != null) {
            sb.append("Brukernavn: " + student.getLogin() + "<br/>");
            sb.append("E-post: " + student.getEmail() + "<br/>");
        } else {
            sb.append("Brukernavn: " + student.getEmail() + "<br/>");
        }
        sb.append("Passord: " + student.getTmpPassword() + "<br/>"
                + "Det er fint om du logger på og godtar invitasjonen.<br/><br/>");

        if (classRoom.getSocialGroup() != null && classRoom.getSocialGroup().length() > 2) {
            sb.append("<b>Facebook</b><br/>"
                    + "Meld deg inn i Facebook gruppen her:<br/>" +
                    "<a href=\"" + classRoom.getSocialGroup() + "\">" + classRoom.getSocialGroup() + "</a><br/><br/>");
        }

        sb.append("<b>Nettklasserom</b><br/>"
                + "Vi bruker Zoom til live nettundervisning.<br/>"
                + "Lenke til Zoom møte blir publisert i Canvas.<br/>"
                + "Vi anbefaler å bruke headset for best mulig lyd.");

        sb.append("<p>Si ifra hvis du trenger hjelp til innlogging.");
        sb.append("<br>Fagskolen i Vestfold og Telemark hjemmeside: <a href='http://f-vt.no'>se f-vt.no</a></p>");

        sb.append("Med vennlig hilsen<br/>"
                + "André Mathisen");


        emailSender.sendSimpleMessage(email, login, "Fagskolen " + classRoom.getShortName(), sb.toString());
    }

}