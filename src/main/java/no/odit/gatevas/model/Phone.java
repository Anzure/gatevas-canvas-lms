package no.odit.gatevas.model;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Deprecated
public class Phone {

	@Id
	@GeneratedValue
	@Type(type="uuid-char")
	private UUID id;
	
	@OneToOne(mappedBy="phone", fetch = FetchType.EAGER)
	private Student student;

	@Column(nullable = false)
	private int countryCode;

	@Column(nullable = false)
	private int phoneNumber;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public int getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(int countryCode) {
		this.countryCode = countryCode;
	}

	public int getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(int phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		String fullCountryCode = String.valueOf(countryCode);
		while(fullCountryCode.length() < 4) {
			fullCountryCode = " " + fullCountryCode;
		}
		String fullPhoneNumber = fullCountryCode + String.valueOf(phoneNumber);
		return fullPhoneNumber;
	}

	public String toBeautifulString() {
		String fullPhoneNumber = "+" + countryCode + " " + phoneNumber;
		return fullPhoneNumber;
	}
}