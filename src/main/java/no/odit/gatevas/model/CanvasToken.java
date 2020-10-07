package no.odit.gatevas.model;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class CanvasToken {

	@Id
	@GeneratedValue
	@Type(type="uuid-char")
	private UUID id;

	@Column(nullable = false)
	private String accessToken;

	@Column(nullable = false)
	private String refreshToken;

	@Column(nullable = false)
	private LocalDateTime accessExpired;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public LocalDateTime getAccessExpired() {
		return accessExpired;
	}

	public void setAccessExpired(LocalDateTime accessExpired) {
		this.accessExpired = accessExpired;
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

	public CanvasToken ifExpired(Function<CanvasToken, CanvasToken> functionalInterface) {
		try {
			return functionalInterface.apply(this);
		} catch (Exception ex) {
			throw new Error("Failed to refresh or generate token!");
		}
	}

	public boolean isExpired() {
		return LocalDateTime.now().plusMinutes(5).isAfter(accessExpired);
	}
}
