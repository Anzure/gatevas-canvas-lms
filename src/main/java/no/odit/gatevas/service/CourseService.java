package no.odit.gatevas.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import no.odit.gatevas.dao.CourseRepo;
import no.odit.gatevas.misc.GoogleSheetIntegration;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;
import no.odit.gatevas.model.Classroom;

@Service
public class CourseService {

	private static final Logger log = LoggerFactory.getLogger(CourseService.class);

	@Autowired
	private CourseRepo courseRepo;

	@Autowired
	private GoogleSheetIntegration googleSheetIntegration;

	/**
	 * Imports students from online Google Sheets
	 * @param course Course to import for
	 * @return May be populated with a list of imported students
	 */
	public Optional<List<Student>> importStudents(Classroom course) {
		try {
			List<Student> students = googleSheetIntegration.processSheet(course.getGoogleSheetId());
			return Optional.of(students);
		} catch (Exception ex) {
			log.error("Failed to import students.", ex);
			return Optional.empty();
		}
	}

	/**
	 * Saves changes for course to storage
	 * @param course Course to save
	 */
	public void saveChanges(Classroom course) {
		courseRepo.saveAndFlush(course);
	}

	/**
	 * Creates a new course in storage
	 * @param course Course to create
	 * @return Newly created course
	 */
	public Classroom addCourse(Classroom course) {
		course.setCanvasStatus(CanvasStatus.UNKNOWN);
		course = courseRepo.saveAndFlush(course);
		log.info("CREATE COURSE -> " + course.toString());
		return course;
	}

	/**
	 * Deletes a course from storage
	 * @param course Course to remove
	 */
	public void removeCourse(Classroom course) {
		log.info("DELETE COURSE -> " + course.toString());
		courseRepo.delete(course);
	}

	/**
	 * Find all courses
	 * @return A list of all courses
	 */
	public List<Classroom> getAllCourses() {
		return courseRepo.findAll();
	}

	/**
	 * Gets course from storage
	 * @param name Search by course name
	 * @return May be populated with an existing course
	 */
	public Optional<Classroom> getCourse(String name){
		return Optional.of(courseRepo.findByShortName(name)
				.orElse(courseRepo.findByLongName(name).orElse(null)));
	}
}