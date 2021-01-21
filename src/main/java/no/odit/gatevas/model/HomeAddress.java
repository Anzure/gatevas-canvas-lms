package no.odit.gatevas.model;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class HomeAddress {

	@Id
	@GeneratedValue
	@Type(type="uuid-char")
	private UUID id;

	@OneToOne
	@JoinColumn(name="student_id", nullable=false)
	private Student student;

	@Column(nullable = false)
	private String streetAddress;

	@Column(nullable = false)
	private Integer zipCode;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

}