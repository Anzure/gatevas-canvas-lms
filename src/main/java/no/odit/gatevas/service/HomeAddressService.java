package no.odit.gatevas.service;

import no.odit.gatevas.dao.HomeAddressRepo;
import no.odit.gatevas.model.HomeAddress;
import no.odit.gatevas.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomeAddressService {

    @Autowired
    private HomeAddressRepo homeAddressRepo;

    public void updateHomeAddress(Student student, String streetAddress, int zipCode, String city) {
        homeAddressRepo.findByStudent(student).ifPresentOrElse(address -> {
            // Update current home address
            address.setCity(city);
            address.setZipCode(zipCode);
            address.setStreetAddress(streetAddress);
            homeAddressRepo.saveAndFlush(address);
        }, () -> {
            // Create new home address
            HomeAddress address = HomeAddress.builder()
                    .student(student)
                    .streetAddress(streetAddress)
                    .zipCode(zipCode)
                    .city(city)
                    .build();
            homeAddressRepo.saveAndFlush(address);
        });
    }

}