package no.odit.gatevas.dao;

import no.odit.gatevas.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepo extends JpaRepository<Student, UUID> {

    Optional<Student> findByFirstNameAndLastName(String firstName, String lastName);

    @Query(value = "SELECT * FROM student WHERE CONCAT(first_name, ' ', last_name) LIKE :fullname", nativeQuery = true)
    Optional<Student> findByFullname(String fullname);

    Optional<Student> findByEmail(String email);

}