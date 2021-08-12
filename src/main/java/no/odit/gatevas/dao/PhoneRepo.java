package no.odit.gatevas.dao;

import no.odit.gatevas.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PhoneRepo extends JpaRepository<Phone, UUID> {

}