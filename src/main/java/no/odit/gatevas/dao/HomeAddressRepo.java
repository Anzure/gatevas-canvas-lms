package no.odit.gatevas.dao;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import no.odit.gatevas.model.HomeAddress;
import no.odit.gatevas.model.Student;

@Repository
public interface HomeAddressRepo extends JpaRepository<HomeAddress, UUID> {

	Optional<HomeAddress> findByStudent(Student student);

}