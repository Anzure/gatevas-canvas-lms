package no.odit.gatevas.dao;

import no.odit.gatevas.model.HomeAddress;
import no.odit.gatevas.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HomeAddressRepo extends JpaRepository<HomeAddress, UUID> {

    Optional<HomeAddress> findByStudent(Student student);

}