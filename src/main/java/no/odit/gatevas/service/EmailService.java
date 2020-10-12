package no.odit.gatevas.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import no.odit.gatevas.misc.EmailSender;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private EmailSender emailSender;

	@Value("${mail.smtp.email}")
	private String testEmail;

	@Autowired
	private StudentService studentSerivce;

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private CanvasService canvasService;

	public void sendEmail(Classroom classRoom) {

		canvasService.syncUsersReadOnly(classRoom);

		List<Student> students = classRoom.getEnrollments().stream()
				.filter(enrollment -> !enrollment.isEmailSent() && enrollment.getStudent().getCanvasStatus() == CanvasStatus.EXISTS)
				.map(RoomLink::getStudent).collect(Collectors.toList());

		for (RoomLink enrollment : classRoom.getEnrollments()) {

			if (enrollment.isEmailSent()) {
				continue;
			}

			Student student = enrollment.getStudent();
			if (student.getCanvasStatus() != CanvasStatus.EXISTS) {
				continue;
			}

			enrollment.setEmailSent(true);
			enrollmentService.saveChanges(enrollment);

			sendEmail(classRoom, student, false);

		}
	}

	public void sendEmail(Classroom classRoom, Student student, boolean isTest) {

		String email = isTest ? testEmail : "waremanu@gmail.com"; //TODO


		StringBuilder sb = new StringBuilder("Hei<br/><br/>");

		sb.append("<b>Canvas</b><br/>"
				+ "Innlogging: <a href=\"https://fagskolentelemark.instructure.com/login/canvas\">fagskolentelemark.instructure.com/login/canvas</a><br/>"
				+ "Login: " + student.getEmail() + "<br/>");

		if (student.isExportedToCSV() && !student.isLoginInfoSent()) {
			sb.append("Passord: " + student.getTmpPassword() + "<br/>");

			student.setLoginInfoSent(true);
			studentSerivce.saveChanges(student);
		}
		sb.append("<br/>");

		if (classRoom.getSocialGroup() != null && classRoom.getSocialGroup().length() > 2) {
			sb.append("<b>Facebook</b><br/>"
					+ "Meld deg inn i Facebook grouppen her: <a href=\"" + classRoom.getSocialGroup() + "\">" + classRoom.getSocialGroup() + "</a><br/><br/><br/>");
		}

		if (classRoom.getCommunicationLink() != null && classRoom.getCommunicationLink().length() > 2) {
			sb.append("<b>Web klasserommet</b><br/>"
					+ "Kobling: <a href=\"" + classRoom.getCommunicationLink() + "\">" + classRoom.getCommunicationLink() + "</a><br/>"
					+ "(logg inn som gjest med ditt navn)<br/><br/>");
		}

		sb.append("<table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td style=\"vertical-align: middle;\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td><h3 color=\"#000000\" class=\"sc-fBuWsC eeihxG\" style=\"margin: 0px; font-size: 16px; color: rgb(0, 0, 0);\"><span>Andre</span><span>&nbsp;</span><span>Mathisen</span></h3><p color=\"#000000\" font-size=\"small\" class=\"sc-fMiknA bxZCMx\" style=\"margin: 0px; color: rgb(0, 0, 0); font-size: 12px; line-height: 20px;\"><span>Konsulent</span></p></td><td width=\"15\"><div></div></td><td color=\"#00960a\" direction=\"vertical\" width=\"1\" class=\"sc-jhAzac hmXDXQ\" style=\"width: 1px; border-bottom: none; border-left: 1px solid rgb(0, 150, 10);\"></td><td width=\"15\"><div></div></td><td><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr height=\"25\" style=\"vertical-align: middle;\"><td width=\"30\" style=\"vertical-align: middle;\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td style=\"vertical-align: bottom;\"><span color=\"#00960a\" width=\"11\" class=\"sc-jlyJG bbyJzT\" style=\"display: block; background-color: rgb(0, 150, 10);\"><img src=\"https://cdn2.hubspot.net/hubfs/53/tools/email-signature-generator/icons/phone-icon-2x.png\" color=\"#00960a\" width=\"13\" class=\"sc-iRbamj blSEcj\" style=\"display: block; background-color: rgb(0, 150, 10);\"></span></td></tr></tbody></table></td><td style=\"padding: 0px; color: rgb(0, 0, 0);\"><a href=\"tel:+47 456 60 785\" color=\"#000000\" class=\"sc-gipzik iyhjGb\" style=\"text-decoration: none; color: rgb(0, 0, 0); font-size: 12px;\"><span>+47 456 60 785</span></a></td></tr><tr height=\"25\" style=\"vertical-align: middle;\"><td width=\"30\" style=\"vertical-align: middle;\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td style=\"vertical-align: bottom;\"><span color=\"#00960a\" width=\"11\" class=\"sc-jlyJG bbyJzT\" style=\"display: block; background-color: rgb(0, 150, 10);\"><img src=\"https://cdn2.hubspot.net/hubfs/53/tools/email-signature-generator/icons/email-icon-2x.png\" color=\"#00960a\" width=\"13\" class=\"sc-iRbamj blSEcj\" style=\"display: block; background-color: rgb(0, 150, 10);\"></span></td></tr></tbody></table></td><td style=\"padding: 0px;\"><a href=\"mailto:andre.mathisen@odit.no\" color=\"#000000\" class=\"sc-gipzik iyhjGb\" style=\"text-decoration: none; color: rgb(0, 0, 0); font-size: 12px;\"><span>andre.mathisen@odit.no</span></a></td></tr><tr height=\"25\" style=\"vertical-align: middle;\"><td width=\"30\" style=\"vertical-align: middle;\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td style=\"vertical-align: bottom;\"><span color=\"#00960a\" width=\"11\" class=\"sc-jlyJG bbyJzT\" style=\"display: block; background-color: rgb(0, 150, 10);\"><img src=\"https://cdn2.hubspot.net/hubfs/53/tools/email-signature-generator/icons/link-icon-2x.png\" color=\"#00960a\" width=\"13\" class=\"sc-iRbamj blSEcj\" style=\"display: block; background-color: rgb(0, 150, 10);\"></span></td></tr></tbody></table></td><td style=\"padding: 0px;\"><a href=\"//www.odit.no\" color=\"#000000\" class=\"sc-gipzik iyhjGb\" style=\"text-decoration: none; color: rgb(0, 0, 0); font-size: 12px;\"><span>www.odit.no</span></a></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table>");

		emailSender.sendSimpleMessage(email, "Velkommen til " + classRoom.getShortName(), sb.toString());
	}
}