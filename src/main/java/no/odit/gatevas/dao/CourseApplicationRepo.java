package no.odit.gatevas.dao;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import no.odit.gatevas.model.CourseApplication;

@Repository
public interface CourseApplicationRepo extends JpaRepository<CourseApplication, UUID> {

}