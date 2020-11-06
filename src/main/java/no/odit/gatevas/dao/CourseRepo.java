package no.odit.gatevas.dao;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.CourseType;

@Repository
public interface CourseRepo extends JpaRepository<Classroom, UUID> {

	List<Classroom> findByType(CourseType type);
	
}