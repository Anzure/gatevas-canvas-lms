package no.odit.gatevas.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import no.odit.gatevas.dao.CourseRepo;
import no.odit.gatevas.misc.GoogleSheetIntegration;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.model.Classroom;

@Service
public class CourseService {

	private static final Logger log = LoggerFactory.getLogger(CourseService.class);

	@Autowired
	private CourseRepo courseRepo;

	@Autowired
	private GoogleSheetIntegration googleSheetIntegration;

	@Autowired
	private EnrollmentService enrollmentService;

	public Optional<List<Student>> importStudents(Classroom course) {
		try {
			List<Student> students = googleSheetIntegration.processSheet(course.getGoogleSheetId());

			List<RoomLink> enrollments = enrollmentService.enrollStudent(students, course); //TODO

			return Optional.of(students);
		} catch (Exception ex) {
			log.error("Failed to import students.", ex);
			return Optional.empty();
		}
	}

	public Classroom addCourse(Classroom course) {
		course = courseRepo.saveAndFlush(course);
		log.info("CREATE COURSE -> " + course.toString());
		return course;
	}

	public void removeCourse(Classroom course) {
		log.info("DELETE COURSE -> " + course.toString());
		courseRepo.delete(course);
	}

	public List<Classroom> getAllCourses() {
		return courseRepo.findAll();
	}

	public Optional<Classroom> getCourse(String name){
		return Optional.of(courseRepo.findByShortName(name)
				.orElse(courseRepo.findByLongName(name).orElse(null)));
	}

}
