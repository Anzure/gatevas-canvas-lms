package no.odit.gatevas.model;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import no.odit.gatevas.type.ApplicationStatus;

@Entity
@Getter @Setter
public class CourseApplication {

	@Id
	@GeneratedValue
	@Type(type="uuid-char")
	private UUID id;

	@ManyToOne
	@JoinColumn(name="student_id", nullable=false)
	private Student student;

	@ManyToOne
	@JoinColumn(name="course_id", nullable=false)
	private CourseType course;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApplicationStatus status;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

}