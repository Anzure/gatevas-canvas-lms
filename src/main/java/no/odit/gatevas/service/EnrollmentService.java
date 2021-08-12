package no.odit.gatevas.service;

import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.dao.EnrollmentRepo;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class EnrollmentService {

	@Autowired
	private EnrollmentRepo enrollmentRepo;

	/**
	 * Enrolls students to a course (in storage)
	 * @param students Student to enroll
	 * @param course Course to enroll students in
	 * @return A list of created enrollments
	 */
	public List<RoomLink> enrollStudent(Set<Student> students, Classroom course) {

		List<RoomLink> enrollments = new ArrayList<RoomLink>();
		for (Student student : students) {
			RoomLink enrollment = createEnrollment(student, course);
			enrollments.add(enrollment);
		}

		return enrollments;		
	}

	/**
	 * Saves changes in enrollment to storage
	 * @param roomLink Enrollment to save
	 */
	public void saveChanges(RoomLink roomLink) {
		enrollmentRepo.saveAndFlush(roomLink);
	}

	/**
	 * Crates a new enrollment for student
	 * @param student Student to enroll
	 * @param course Course to enroll student in
	 * @return Newly created enrollment
	 */
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

	/**
	 * Gets enrollment from storage
	 * @param student Student to search by
	 * @param course Course to search in
	 * @return Empty Optional or populated with existing enrollment
	 */
	public Optional<RoomLink> getEnrollment(Student student, Classroom course) {
		return enrollmentRepo.findByStudentAndCourse(student, course);
	}
}