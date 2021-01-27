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
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import no.odit.gatevas.type.CanvasStatus;

@Entity(name = "Enrollment")
@Table(name = "enrollment")
@Getter @Setter
public class RoomLink {

	@Id
	@GeneratedValue
	@Type(type="uuid-char")
	private UUID id;

	@Column(nullable = false)
	private Boolean emailSent;

	@Column(nullable = false)
	private Boolean textSent;

	@ManyToOne
	@JoinColumn(name="student_id", nullable=false)
	private Student student;

	@ManyToOne
	@JoinColumn(name="course_id", nullable=false)
	private Classroom course;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CanvasStatus canvasStatus;

	@Column(nullable = true)
	private Long canvasId;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Override
	public String toString() {
		return "RoomLink [id=" + id + ", emailSent=" + emailSent + ", textSent=" + textSent + ", student=" + student
				+ ", canvasStatus=" + canvasStatus + ", updatedAt=" + updatedAt + ", createdAt="
				+ createdAt + "]";
	}

}