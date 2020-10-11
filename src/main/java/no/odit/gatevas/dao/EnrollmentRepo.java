package no.odit.gatevas.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.model.Classroom;

@Repository
public interface EnrollmentRepo extends JpaRepository<RoomLink, UUID> {

	Optional<RoomLink> findByStudentAndCourse(Student student, Classroom course);

	List<RoomLink> findByStudent(Student student);

	List<RoomLink> findByCourse(Classroom course);

}