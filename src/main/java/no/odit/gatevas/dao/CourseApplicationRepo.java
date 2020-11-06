package no.odit.gatevas.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import no.odit.gatevas.model.CourseApplication;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.ApplicationStatus;

@Repository
public interface CourseApplicationRepo extends JpaRepository<CourseApplication, UUID> {

	Optional<CourseApplication> findByStudentAndCourse(Student student, CourseType course);
	
	List<CourseApplication> findByStatus(ApplicationStatus status);
	
	List<CourseApplication> findByStatusAndCourse(ApplicationStatus status, CourseType course);
	
	List<CourseApplication> findByCourse(CourseType course);

}