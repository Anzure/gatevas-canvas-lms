package no.odit.gatevas.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import no.odit.gatevas.model.Classroom;

@Repository
public interface CourseRepo extends JpaRepository<Classroom, UUID> {

	Optional<Classroom> findByShortName(String name);

	Optional<Classroom> findByLongName(String name);

}