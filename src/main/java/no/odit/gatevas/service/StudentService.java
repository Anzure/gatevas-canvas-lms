package no.odit.gatevas.service;

import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.dao.StudentRepo;
import no.odit.gatevas.misc.GeneralUtil;
import no.odit.gatevas.misc.SheetExportCSV;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.Phone;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;
import no.odit.gatevas.type.StudentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentService {

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private PhoneService phoneService;

    @Autowired
    private SheetExportCSV sheetExportCSV;

    @Autowired
    private CanvasService canvasService;

    // Creates a new student or get existing
    public Student createStudent(String email, String firstName, String lastName, LocalDate birth, Integer phoneNumber) {

        // Detect and throw decoding error
        if (firstName.matches("[^a-zA-Z0-9 ÆØÅæøå]") || lastName.matches("[^a-zA-Z0-9 ÆØÅæøå]")
                || firstName.contains("�") || lastName.contains("�")) {
            throw new Error("Error in charset decoding for " + firstName + " " + lastName + ".");
        }

        // Return existing student (email)
        Optional<Student> existingEmail = getUserByEmail(email);
        if (existingEmail.isPresent()) {
            log.debug("EMAIL ALREADY EXIST -> " + existingEmail.get());
            return fixStudentDetails(existingEmail.get(), firstName, lastName, email);
        }

        // Return existing student (name & birth)
        Optional<Student> existingName = getUserByNameAndBirth(firstName, lastName, birth);
        if (existingName.isPresent()) {
            log.debug("NAME ALREADY EXIST -> " + existingName.get());
            return fixStudentDetails(existingName.get(), firstName, lastName, email);
        }

        // Create new student
        Phone phone = phoneService.createPhone(phoneNumber);
        Student student = new Student();
        student.setEmail(email.trim());
        student.setFirstName(firstName.trim());
        student.setLastName(lastName.trim());
        student.setBirthDate(birth);
        student.setTmpPassword(GeneralUtil.generatePassword());
        student.setPhone(phone);
        student.setLoginInfoSent(false);
        student.setExportedToCSV(false);
        student.setCanvasStatus(CanvasStatus.UNKNOWN);
        student.setStudentStatus(StudentStatus.ALLOWED);
        student = studentRepo.saveAndFlush(student);
        log.debug("CREATED STUDENT -> " + student);
        return student;
    }

    // Fix student name and email address
    public Student fixStudentDetails(Student student, String firstName, String lastName, String emailAddress) {
        if (student.getFullName().matches("[^a-zA-Z0-9 ÆØÅæøå]") || student.getFullName().contains("�")) {
            log.warn("Detected encoding error in student details for " + student.getFullName() + ".");
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student = studentRepo.saveAndFlush(student);
            log.info("Fixed encoding in student details for " + firstName + " " + lastName + ".");
        }
        if (!student.getEmail().equalsIgnoreCase(emailAddress)) {
            student.setLogin(student.getEmail());
            student.setEmail(emailAddress);
            student = studentRepo.saveAndFlush(student);
            log.info("Updated email address for " + firstName + " " + lastName + " to " + emailAddress + ".");
        }
        return student;
    }

    // Exports students in course to a CSV file
    public boolean exportStudentsToCSV(Classroom course, File file) {

        // Sync students
        canvasService.syncUsersReadOnly(course);

        // Filter out existing students
        List<Student> students = course.getStudents().stream()
                .filter(student -> !student.getExportedToCSV() && student.getCanvasStatus() == CanvasStatus.MISSING)
                .collect(Collectors.toList());
        if (students.size() <= 0) {
            log.debug("All students in '" + course.getShortName() + "' already exists in Canvas LMS.");
            return true;
        }

        // Update status
        students.forEach(student -> {
            student.setExportedToCSV(true);
            saveChanges(student);
        });

        // Create CSV file
        try {
            sheetExportCSV.createCSVFile(file, students);
            return true;
        } catch (IOException e) {
            log.error("Failed to create CSV file.", e);
            return false;
        }
    }

    // Save student to storage
    public void saveChanges(Student student) {
        studentRepo.saveAndFlush(student);
    }

    // Get student from storage by name
    @Deprecated
    public Optional<Student> getUserByName(String firstName, String lastName) {
        return studentRepo.findByFirstNameAndLastName(firstName.trim(), lastName.trim());
    }

    // Get student from storage by name and birth
    public Optional<Student> getUserByNameAndBirth(String firstName, String lastName, LocalDate birthDate) {
        return studentRepo.findByFirstNameAndLastNameAndBirthDate(firstName.trim(), lastName.trim(), birthDate);
    }

    // Get student from storage by email
    public Optional<Student> getUserByEmail(String email) {
        return studentRepo.findByEmail(email.trim());
    }

    // Get student from storage by full name
    @Deprecated
    public Optional<Student> getUserByFullName(String fullName) {
        return studentRepo.findByFullName(fullName.trim());
    }

    // Get student from storage by full name and birth
    public Optional<Student> getUserByFullNameAndBirth(String fullName, LocalDate birthDate) {
        return studentRepo.findByFullNameAndBirthDate(fullName.trim(), birthDate);
    }

}