package no.odit.gatevas.service;

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

	/**
	 * Sends email to students in course with login and information.
	 * @param classRoom Course that members shall be informed
	 */
	public void sendEmail(Classroom classRoom) {

		canvasService.syncUsersReadOnly(classRoom);

		for (RoomLink enrollment : classRoom.getEnrollments()) {

			if (enrollment.isEmailSent()) {
				continue;
			}

			Student student = enrollment.getStudent();
			if (student.getCanvasStatus() != CanvasStatus.EXISTS) {
				continue;
			}

			if (enrollment.getCanvasStatus() != CanvasStatus.EXISTS) {
				continue;
			}

			enrollment.setEmailSent(true);
			enrollmentService.saveChanges(enrollment);

			sendEmail(classRoom, student, false);

		}
	}

	/**
	 * Send email with login and information to student.
	 * @param classRoom Course that is relevant for email
	 * @param student Student that will receive email
	 * @param isTest If it shall be a test.
	 */
	public void sendEmail(Classroom classRoom, Student student, boolean isTest) {

		String email = isTest ? contactEmail : student.getEmail();

		StringBuilder sb = new StringBuilder("<p>Hei</p>");

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
					+ "Meld deg inn i Facebook gruppen her: <a href=\"" + classRoom.getSocialGroup() + "\">" + classRoom.getSocialGroup() + "</a><br/><br/>");
		}

		if (classRoom.getCommunicationLink() != null && classRoom.getCommunicationLink().length() > 2) {
			sb.append("<b>Web klasserommet</b><br/>"
					+ "Kobling: <a href=\"" + classRoom.getCommunicationLink() + "\">" + classRoom.getCommunicationLink() + "</a><br/>"
					+ "(logg inn som gjest med ditt navn)<br/>");
		}

		sb.append("<p>Fagskolen i Vestfold og Telemark hjemmeside: <a href='http://f-vt.no'>se f-vt.no</a></p>");
		sb.append("<p>Si ifra hvis du trenger hjelp til innlogging.</p>");

		sb.append("Med vennlig hilsen");
		sb.append("<table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td style=\"vertical-align: middle;\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td><h3 color=\"#000000\" class=\"sc-fBuWsC eeihxG\" style=\"margin: 0px; font-size: 16px; color: rgb(0, 0, 0);\"><span>Andre</span><span>&nbsp;</span><span>Mathisen</span></h3><p color=\"#000000\" font-size=\"small\" class=\"sc-fMiknA bxZCMx\" style=\"margin: 0px; color: rgb(0, 0, 0); font-size: 12px; line-height: 20px;\"><span>Konsulent</span></p></td><td width=\"15\"><div></div></td><td color=\"#ff914d\" direction=\"vertical\" width=\"1\" class=\"sc-jhAzac hmXDXQ\" style=\"width: 1px; border-bottom: none; border-left: 1px solid rgb(255, 145, 77);\"></td><td width=\"15\"><div></div></td><td><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr height=\"25\" style=\"vertical-align: middle;\"><td width=\"30\" style=\"vertical-align: middle;\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td style=\"vertical-align: bottom;\"><span color=\"#ff914d\" width=\"11\" class=\"sc-jlyJG bbyJzT\" style=\"display: block; background-color: rgb(255, 145, 77);\"><img src=\"https://cdn2.hubspot.net/hubfs/53/tools/email-signature-generator/icons/phone-icon-2x.png\" color=\"#ff914d\" width=\"13\" class=\"sc-iRbamj blSEcj\" style=\"display: block; background-color: rgb(255, 145, 77);\"></span></td></tr></tbody></table></td><td style=\"padding: 0px; color: rgb(0, 0, 0);\"><a href=\"tel:+47 456 60 785\" color=\"#000000\" class=\"sc-gipzik iyhjGb\" style=\"text-decoration: none; color: rgb(0, 0, 0); font-size: 12px;\"><span>+47 456 60 785</span></a></td></tr><tr height=\"25\" style=\"vertical-align: middle;\"><td width=\"30\" style=\"vertical-align: middle;\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td style=\"vertical-align: bottom;\"><span color=\"#ff914d\" width=\"11\" class=\"sc-jlyJG bbyJzT\" style=\"display: block; background-color: rgb(255, 145, 77);\"><img src=\"https://cdn2.hubspot.net/hubfs/53/tools/email-signature-generator/icons/email-icon-2x.png\" color=\"#ff914d\" width=\"13\" class=\"sc-iRbamj blSEcj\" style=\"display: block; background-color: rgb(255, 145, 77);\"></span></td></tr></tbody></table></td><td style=\"padding: 0px;\"><a href=\"mailto:andre@odit.no\" color=\"#000000\" class=\"sc-gipzik iyhjGb\" style=\"text-decoration: none; color: rgb(0, 0, 0); font-size: 12px;\"><span>andre@odit.no</span></a></td></tr><tr height=\"25\" style=\"vertical-align: middle;\"><td width=\"30\" style=\"vertical-align: middle;\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"sc-gPEVay eQYmiW\" style=\"vertical-align: -webkit-baseline-middle; font-size: small; font-family: Arial;\"><tbody><tr><td style=\"vertical-align: bottom;\"><span color=\"#ff914d\" width=\"11\" class=\"sc-jlyJG bbyJzT\" style=\"display: block; background-color: rgb(255, 145, 77);\"><img src=\"https://cdn2.hubspot.net/hubfs/53/tools/email-signature-generator/icons/link-icon-2x.png\" color=\"#ff914d\" width=\"13\" class=\"sc-iRbamj blSEcj\" style=\"display: block; background-color: rgb(255, 145, 77);\"></span></td></tr></tbody></table></td><td style=\"padding: 0px;\"><a href=\"//www.odit.no\" color=\"#000000\" class=\"sc-gipzik iyhjGb\" style=\"text-decoration: none; color: rgb(0, 0, 0); font-size: 12px;\"><span>www.odit.no</span></a></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table>");

		emailSender.sendSimpleMessage(email, "Fagskolen " + classRoom.getShortName(), sb.toString());
	}
}