package no.odit.gatevas.dao;

import no.odit.gatevas.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepo extends JpaRepository<Student, UUID> {

    @Deprecated
    Optional<Student> findByFirstNameAndLastName(String firstName, String lastName);

    Optional<Student> findByFirstNameAndLastNameAndBirthDateIsNull(String firstName, String lastName);

    Optional<Student> findByFirstNameAndLastNameAndBirthDate(String firstName, String lastName, LocalDate birthDate);

    @Query(value = "SELECT * FROM student WHERE CONCAT(first_name, ' ', last_name) = :fullName AND birth_date = :birthDate", nativeQuery = true)
    Optional<Student> findByFullNameAndBirthDate(String fullName, LocalDate birthDate);

    @Deprecated
    @Query(value = "SELECT * FROM student WHERE CONCAT(first_name, ' ', last_name) = :fullName", nativeQuery = true)
    Optional<Student> findByFullName(String fullName);

    Optional<Student> findByEmail(String email);

    Optional<Student> findByLoginIsNotNullAndLogin(String login);

}