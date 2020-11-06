package no.odit.gatevas.dao;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import no.odit.gatevas.model.CourseType;

@Repository
public interface CourseTypeRepo extends JpaRepository<CourseType, UUID> {

	Optional<CourseType> findByShortName(String name);

	Optional<CourseType> findByLongName(String name);
	
}