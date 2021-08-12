package no.odit.gatevas.dao;

import no.odit.gatevas.model.CourseApplication;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseApplicationRepo extends JpaRepository<CourseApplication, UUID> {

	Optional<CourseApplication> findByStudentAndCourse(Student student, CourseType course);
	
	List<CourseApplication> findByStatus(ApplicationStatus status);
	
	List<CourseApplication> findByStatusAndCourse(ApplicationStatus status, CourseType course);
	
	List<CourseApplication> findByCourse(CourseType course);

}