package no.odit.gatevas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;
import no.odit.gatevas.type.CanvasStatus;
import no.odit.gatevas.type.StudentStatus;

@Entity
@Getter @Setter
public class Student {

	@Id
	@GeneratedValue
	@Type(type="uuid-char")
	private UUID id;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false, unique = true)
	private String email;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="phone_id", nullable=true)
	private Phone phone;

	@Column(nullable = true)
	private LocalDate birthDate;

	@Column(nullable = false)
	private String tmpPassword;

	@Column(nullable = false)
	private Boolean loginInfoSent;

	@Column(nullable = false, name = "exported_to_csv")
	private Boolean exportedToCSV;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CanvasStatus canvasStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private StudentStatus studentStatus;

	@Column(nullable = true)
	private Integer canvasId;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@OneToOne(mappedBy = "student")
	private HomeAddress homeAddress;

	@OneToMany(mappedBy = "student")
	private Set<RoomLink> enrollments;

	@Deprecated
	public Boolean isLoginInfoSent() {
		return loginInfoSent;
	}

	@Deprecated
	public Boolean isExportedToCSV() {
		return exportedToCSV;
	}

	public String getUserId() {
		return firstName.toLowerCase().replace("æ", "e").replace("ø", "o").replace("å", "a").substring(0, 2)
				+ lastName.toLowerCase().replace("æ", "e").replace("ø", "o").replace("å", "a").substring(0, 2)
				+ "-" + id.toString().split("-")[3];
	}

	@Override
	public String toString() {
		return "Student [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", phone=" + phone + ", tmpPassword=" + tmpPassword + ", loginInfoSent=" + loginInfoSent
				+ ", exportedToCSV=" + exportedToCSV + ", canvasStatus=" + canvasStatus + ", updatedAt=" + updatedAt
				+ ", createdAt=" + createdAt + "]";
	}

}