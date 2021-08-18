package no.odit.gatevas.dao;

import no.odit.gatevas.model.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseTypeRepo extends JpaRepository<CourseType, UUID> {

    Optional<CourseType> findByShortName(String name);

    Optional<CourseType> findByLongName(String name);

}