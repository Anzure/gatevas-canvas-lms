package no.odit.gatevas.model;

import lombok.Getter;
import lombok.Setter;
import no.odit.gatevas.type.CanvasStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity(name = "Course")
@Table(name = "course")
@Getter @Setter
public class Classroom {

	@Id
	@GeneratedValue
	@Type(type="uuid-char")
	private UUID id;

	@ManyToOne
	@JoinColumn(name="type_id", nullable=false)
	private CourseType type;

	@Column(nullable = false)
	private String period;

	@Column
	private String socialGroup;

	@Column(nullable = false)
	private String communicationLink;

	@Column(nullable = false)
	private String googleSheetId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CanvasStatus canvasStatus;

	@Column
	private Integer canvasId;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "course")
	private Set<RoomLink> enrollments;

	public String getShortName() {
		return type.getShortName() + "-" + period;
	}

	public String getLongName() {
		return type.getLongName() + " " + period;
	}

	public List<Student> getStudents() {
		return enrollments.stream().map(RoomLink::getStudent).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return "Classroom [id=" + id + ", period=" + period + ", socialGroup=" + socialGroup + ", communicationLink="
				+ communicationLink + ", googleSheetId=" + googleSheetId + ", canvasStatus=" + canvasStatus
				+ ", canvasId=" + canvasId + ", updatedAt=" + updatedAt + ", createdAt=" + createdAt + "]";
	}

}