package no.odit.gatevas.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Phone {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID id;

    @OneToOne(mappedBy = "phone", fetch = FetchType.EAGER)
    private Student student;

    @Column(nullable = false)
    private Integer countryCode;

    @Column(nullable = false)
    private Integer phoneNumber;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        String fullCountryCode = String.valueOf(countryCode);
        while (fullCountryCode.length() < 4) {
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