package no.odit.gatevas.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.dao.CourseApplicationRepo;
import no.odit.gatevas.dao.CourseRepo;
import no.odit.gatevas.dao.CourseTypeRepo;
import no.odit.gatevas.misc.GoogleSheetIntegration;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.ApplicationStatus;
import no.odit.gatevas.type.CanvasStatus;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.CourseApplication;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.RoomLink;

@Service
@Slf4j
public class CourseService {

	@Autowired
	private CourseRepo courseRepo;

	@Autowired
	private CourseTypeRepo courseTypeRepo;

	@Autowired
	private GoogleSheetIntegration googleSheetIntegration;

	/**
	 * Imports students from online Google Sheets
	 * @param course Course to import for
	 * @return May be populated with a list of imported students
	 */
	public Optional<Set<Student>> importStudents(Classroom course) {
		try {
			Set<Student> students = googleSheetIntegration.processSheet(course.getGoogleSheetId(), course.getType());
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
		return getAllCourses().stream()
				.filter(course -> course.getShortName().equalsIgnoreCase(name) || course.getLongName().equalsIgnoreCase(name))
				.findFirst();
	}

	/**
	 * Gets course type from storage
	 * @param name Search by course type name
	 * @return Is null if no result was found
	 */
	public Optional<CourseType> getCourseType(String name) {
		return Optional.ofNullable(courseTypeRepo.findByShortName(name)
				.orElse(courseTypeRepo.findByLongName(name).orElse(null)));
	}

	@Autowired
	private CourseApplicationRepo courseApplicationRepo;

	@Autowired
	private EnrollmentService enrollmentService;

	public List<CourseType> getCourseTypes(){
		return courseTypeRepo.findAll();
	}

	public CourseApplication createCourseApplication(Student student, CourseType courseType) {

		CourseApplication apply = new CourseApplication();
		Optional<CourseApplication> optCourseApply = courseApplicationRepo.findByStudentAndCourse(student, courseType);
		if (optCourseApply.isPresent()) {
			apply = optCourseApply.get();
		}
		else {
			apply.setCourse(courseType);
			apply.setStudent(student);
			apply.setStatus(ApplicationStatus.WAITLIST);
		}

		if (apply.getStatus() == ApplicationStatus.WAITLIST) {
			List<Classroom> courses = courseRepo.findByType(courseType);
			for (Classroom course : courses) {
				Optional<RoomLink> optEnroll = enrollmentService.getEnrollment(student, course);
				if (optEnroll.isPresent()) {
					apply.setStatus(ApplicationStatus.ACCEPTED);
				}
			}

		}

		return courseApplicationRepo.saveAndFlush(apply);
	}
}