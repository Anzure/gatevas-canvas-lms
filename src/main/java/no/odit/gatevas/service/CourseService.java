package no.odit.gatevas.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import no.odit.gatevas.dao.CourseRepo;
import no.odit.gatevas.model.Subject;

@Service
public class CourseService {

	private static final Logger log = LoggerFactory.getLogger(CourseService.class);

	@Autowired
	private CourseRepo courseRepo;

	public Subject addCourse(Subject subject) {
		return courseRepo.saveAndFlush(subject);
	}

	public void removeCourse(Subject subject) {
		courseRepo.delete(subject);
	}

	public List<Subject> getAllCourses() {
		return courseRepo.findAll();
	}

	public Optional<Subject> getCourse(String name){
		return Optional.of(courseRepo.findByShortName(name)
				.orElse(courseRepo.findByLongName(name).orElse(null)));
	}

}
