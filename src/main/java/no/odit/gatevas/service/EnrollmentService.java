package no.odit.gatevas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.odit.gatevas.dao.EnrollmentRepo;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;
import no.odit.gatevas.model.Classroom;

@Service
public class EnrollmentService {

	private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

	@Autowired
	private EnrollmentRepo enrollmentRepo;

	public List<RoomLink> enrollStudent(List<Student> students, Classroom course) {

		List<RoomLink> enrollments = new ArrayList<RoomLink>();
		for (Student student : students) {
			RoomLink enrollment = createEnrollment(student, course);
			enrollments.add(enrollment);
		}

		return enrollments;		
	}

	public void saveChanges(RoomLink roomLink) {
		enrollmentRepo.saveAndFlush(roomLink);
	}

	public RoomLink createEnrollment(Student student, Classroom course) {

		// Existing enrollment
		Optional<RoomLink> existing = getEnrollment(student, course);
		if (existing.isPresent()) {
			log.debug("ENROLLMENT ALREADY EXIST -> " + existing.get().toString());
			return existing.get();
		}

		// Create new enrollment
		RoomLink enrollment = new RoomLink();
		enrollment.setStudent(student);
		enrollment.setCanvasStatus(CanvasStatus.UNKNOWN);
		enrollment.setCourse(course);
		enrollment.setTextSent(false);
		enrollment.setEmailSent(false);
		enrollment = enrollmentRepo.saveAndFlush(enrollment);
		log.debug("CREATE ENROLLMENT -> " + enrollment.toString());
		return enrollment;
	}

	public Optional<RoomLink> getEnrollment(Student student, Classroom course) {
		return enrollmentRepo.findByStudentAndCourse(student, course);
	}
}