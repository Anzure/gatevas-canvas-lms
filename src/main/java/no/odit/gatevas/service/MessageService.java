package no.odit.gatevas.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.Phone;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;

@Service
public class MessageService {

	@Autowired
	private CourseService courseService;

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private StudentService studentService;
	
	@Autowired
	private PhoneService phoneService;

	public void informStudents(Classroom classRoom) {
		
//		String textMsg = "";
//
//		classRoom.getEnrollments().stream().filter(enrollment -> enrollment.getCanvasStatus() != CanvasStatus.EXISTS)
//			.collect(Collectors.toList())
//			.forEach(enrollment -> {
//				enrollment.setEmailSent(true);
//				enrollment.setTextSent(true);
//				enrollmentService.saveChanges(enrollment);
//				Student student = enrollment.getStudent();
//				Phone phone = student.getPhone();
//				phoneService.sendSMS("", phone);
//			});


	}

}
