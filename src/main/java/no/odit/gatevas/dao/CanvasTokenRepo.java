package no.odit.gatevas.dao;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import no.odit.gatevas.model.CanvasToken;

@Repository
public interface CanvasTokenRepo extends JpaRepository<CanvasToken, UUID> {

	Optional<CanvasToken> findTop1ByOrderByCreatedAtDesc();

}