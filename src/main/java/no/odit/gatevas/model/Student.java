package no.odit.gatevas.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
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
	@JoinColumn(name="phone_id", nullable=false)
	private Phone phone;

	@Column(nullable = false)
	private String tmpPassword;

	@Column(nullable = false)
	private boolean loginInfoSent;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "student")
	private Set<RoomLink> enrollments;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Phone getPhone() {
		return phone;
	}

	public void setPhone(Phone phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTmpPassword() {
		return tmpPassword;
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


	public void setTmpPassword(String tmpPassword) {
		this.tmpPassword = tmpPassword;
	}

	public String getUserId() {
		return firstName.substring(0, 1) + lastName.substring(0, 1) + "-" + id.toString().split("-")[3];
	}

	public Set<RoomLink> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(Set<RoomLink> enrollments) {
		this.enrollments = enrollments;
	}

	public boolean isLoginInfoSent() {
		return loginInfoSent;
	}

	public void setLoginInfoSent(boolean loginInfoSent) {
		this.loginInfoSent = loginInfoSent;
	}

	@Override
	public String toString() {
		return "Student [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", phone=" + phone + ", tmpPassword=" + tmpPassword + ", loginInfoSent=" + loginInfoSent
				+ ", updatedAt=" + updatedAt + ", createdAt=" + createdAt + "]";
	}
}