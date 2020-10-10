package no.odit.gatevas.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class Subject {

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
	private Set<Enrollment> enrollments;

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

	public Set<Enrollment> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(Set<Enrollment> enrollments) {
		this.enrollments = enrollments;
	}

	@Override
	public String toString() {
		return "Subject [id=" + id + ", shortName=" + shortName + ", longName=" + longName + ", socialGroup="
				+ socialGroup + ", communicationLink=" + communicationLink + ", googleSheetId=" + googleSheetId
				+ ", enrollments=" + enrollments + "]";
	}
}