package no.odit.gatevas.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import no.odit.gatevas.type.CanvasStatus;

@Entity(name = "Course")
@Table(name = "course")
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

	@Column(nullable = true)
	private String socialGroup;

	@Column(nullable = false)
	private String communicationLink;

	@Column(nullable = false)
	private String googleSheetId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CanvasStatus canvasStatus;

	@Column(nullable = true)
	private int canvasId;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "course")
	private Set<RoomLink> enrollments;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getShortName() {
		return type.getShortName() + "-" + period;
	}

	public String getLongName() {
		return type.getLongName() + " " + period;
	}

	public String getSocialGroup() {
		return socialGroup;
	}

	public void setSocialGroup(String socialGroup) {
		this.socialGroup = socialGroup;
	}

	public String getGoogleSheetId() {
		return googleSheetId;
	}

	public void setGoogleSheetId(String googleSheetId) {
		this.googleSheetId = googleSheetId;
	}

	public String getCommunicationLink() {
		return communicationLink;
	}

	public void setCommunicationLink(String communicationLink) {
		this.communicationLink = communicationLink;
	}

	public Set<RoomLink> getEnrollments() {
		return enrollments;
	}

	public List<Student> getStudents() {
		return enrollments.stream().map(RoomLink::getStudent).collect(Collectors.toList());
	}

	public void setEnrollments(Set<RoomLink> enrollments) {
		this.enrollments = enrollments;
	}

	public CanvasStatus getCanvasStatus() {
		return canvasStatus;
	}

	public void setCanvasStatus(CanvasStatus canvasStatus) {
		this.canvasStatus = canvasStatus;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getCanvasId() {
		return canvasId;
	}

	public void setCanvasId(int canvasId) {
		this.canvasId = canvasId;
	}

	public CourseType getType() {
		return type;
	}

	public void setType(CourseType type) {
		this.type = type;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	@Override
	public String toString() {
		return "Classroom [id=" + id + ", period=" + period + ", socialGroup=" + socialGroup + ", communicationLink="
				+ communicationLink + ", googleSheetId=" + googleSheetId + ", canvasStatus=" + canvasStatus
				+ ", canvasId=" + canvasId + ", updatedAt=" + updatedAt + ", createdAt=" + createdAt + "]";
	}
}