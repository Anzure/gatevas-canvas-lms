package no.odit.gatevas.dao;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import no.odit.gatevas.model.Student;

@Repository
public interface StudentRepo extends JpaRepository<Student, UUID> {

	Optional<Student> findByFirstNameAndLastName(String firstName, String lastName);
	
	Optional<Student> findByEmail(String email);

}