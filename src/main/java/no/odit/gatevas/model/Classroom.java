package no.odit.gatevas.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

@Entity(name = "Course")
@Table(name = "course")
public class Classroom {

	@Id
	@GeneratedValue
	@Type(type="uuid-char")
	private UUID id;

	@Column(nullable = false)
	private String shortName;

	@Column(nullable = false)
	private String longName;

	@Column(nullable = true)
	private String socialGroup;

	@Column(nullable = false)
	private String communicationLink;

	@Column(nullable = false)
	private String googleSheetId;

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
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
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

	@Override
	public String toString() {
		return "Classroom [id=" + id + ", shortName=" + shortName + ", longName=" + longName + ", socialGroup="
				+ socialGroup + ", communicationLink=" + communicationLink + ", googleSheetId=" + googleSheetId
				+ ", updatedAt=" + updatedAt + ", createdAt=" + createdAt + "]";
	}
}