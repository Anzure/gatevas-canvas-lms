package no.odit.gatevas.dao;

import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepo extends JpaRepository<Classroom, UUID> {

    List<Classroom> findByType(CourseType type);

}