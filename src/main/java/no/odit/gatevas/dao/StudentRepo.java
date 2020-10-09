package no.odit.gatevas.dao;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import no.odit.gatevas.model.Student;

@Repository
@Deprecated
public interface StudentRepo extends JpaRepository<Student, UUID> {

}