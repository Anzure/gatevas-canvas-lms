package no.odit.gatevas.model;

import lombok.Getter;
import lombok.Setter;
import no.odit.gatevas.type.CanvasStatus;
import no.odit.gatevas.type.StudentStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column
    private String login;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phone_id")
    private Phone phone;

    @Column
    private LocalDate birthDate;

    @Column
    private String socialSecurityNumber;

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
    @Column
    private StudentStatus studentStatus;

    @Column
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

    public LocalDate getBirthDate() {
        if (birthDate == null) return null;
        if (birthDate.isAfter(LocalDate.now())) birthDate = birthDate.minusYears(100);
        if (birthDate.isBefore(LocalDate.now().minusYears(90)))
            throw new Error("Birth date " + birthDate.toString() + " for student is too old!");
        if (birthDate.isAfter(LocalDate.now().minusYears(15)))
            throw new Error("Birth date " + birthDate.toString() + " for student is too young!");
        return birthDate;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

}