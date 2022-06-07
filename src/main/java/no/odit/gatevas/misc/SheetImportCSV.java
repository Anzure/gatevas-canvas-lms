package no.odit.gatevas.misc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.service.HomeAddressService;
import no.odit.gatevas.service.PhoneService;
import no.odit.gatevas.service.StudentService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class SheetImportCSV {

    @Autowired
    private PhoneService phoneService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private HomeAddressService homeAddressService;

    @Autowired
    private StrongTextEncryptor textEncryptor;

    @SneakyThrows
    public Set<Student> processSheet(File csvFile, CourseType courseType, String charset, boolean useComma) {

        List<Student> students = new ArrayList<>();

        log.info("Proccessing " + csvFile.getName() + " spreadsheet...");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), charset));
        try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(useComma ? ',' : ';'))) {

            List<CSVRecord> records = parser.getRecords();
            log.debug("Found " + records.size() + " records.");
            for (CSVRecord record : records) {
                // Parse education
                String education = record.get("Utdanning");
                String alternative = education.length() > 8 && education.contains(" - ") ? education.split(" - ")[0] : education;
                if (courseType == null) courseType = courseService.getCourseType(education).orElseThrow();
                else if (!education.equalsIgnoreCase(courseType.getLongName()) && !education.equalsIgnoreCase(courseType.getShortName())
                        && !education.equalsIgnoreCase(courseType.getAliasName()) && !alternative.equalsIgnoreCase(courseType.getLongName())
                        && !alternative.equalsIgnoreCase(courseType.getAliasName())) continue;

                // Parse student data
                String firstName = record.get("Fornavn");
                String lastName = record.get("Etternavn");
                String emailAddress = record.get("E-postadresse");

                // Parse phone number
                String phoneInput = record.get("Mobilnummer");
                phoneInput = phoneInput.replaceAll("[^0-9]", "");
                phoneInput = phoneInput.length() > 8 ? phoneInput.substring(phoneInput.length() - 8) : phoneInput;
                Integer phoneNumber = phoneInput.length() > 0 ? Integer.parseInt(phoneInput) : null;

                // Parse birth date
                LocalDate birthDate = null;
                String birthInput = record.isMapped("Fodselsdato") ? record.get("Fodselsdato") : record.get("Personnummer").substring(0, 6);
                if (birthInput != null && birthInput.length() > 5) {
                    try {
                        birthInput = birthInput.replaceAll("[^\\d]", "");
                        if (birthInput.length() == 8)
                            birthInput = birthInput.substring(0, 4) + birthInput.substring(6, 8);
                        if (birthInput.length() == 5) birthInput = "0" + birthInput;
                        birthDate = LocalDate.parse(birthInput, DateTimeFormatter.ofPattern("ddMMyy"));
                        if (birthDate.isAfter(LocalDate.now().minusYears(15))) birthDate = birthDate.minusYears(100);
                    } catch (Exception exception) {
                        log.warn("Failed to parsing birth date for " + firstName + " " + lastName + "."
                                + " Birth date input: '" + birthInput + "'");
                    }
                } else {
                    log.warn("Failed to parsing birth date for " + firstName + " " + lastName + "."
                            + " Birth date input: '" + birthInput + "'");
                }

                // Create new student
                Student student = studentService.createStudent(emailAddress, firstName, lastName, birthDate, phoneNumber);

                // Update phone number
                if (student.getPhone() == null || student.getPhone().getPhoneNumber() == null
                        || student.getPhone().getPhoneNumber() == 0) {
                    phoneService.createPhone(phoneNumber);
                }

                // Update birth date
                LocalDate oldBirthDate = null;
                try {
                    oldBirthDate = student.getBirthDate();
                } catch (Exception exception) {
                    student.setBirthDate(null);
                    studentService.saveChanges(student);
                    log.error("Failed to retrieve stored birth date", exception);
                    System.exit(1);
                }
                if (oldBirthDate == null && birthDate != null) {
                    student.setBirthDate(birthDate);
                    studentService.saveChanges(student);
                }
                if (student.getBirthDate() == null && oldBirthDate == null) {
                    log.error("Failed to parsing birth date for " + student.getFullName() + "."
                            + " Birth date input: '" + birthInput + "'");
                }

                // Social security number
                if (student.getSocialSecurityNumber() == null && record.isMapped("Personnummer")) {
                    String socialSecurityNumber = record.get("Personnummer");
                    if (socialSecurityNumber != null && socialSecurityNumber.length() == 11) {
                        socialSecurityNumber = textEncryptor.encrypt(socialSecurityNumber);
                        student.setSocialSecurityNumber(socialSecurityNumber);
                        studentService.saveChanges(student);
                        log.debug("Updated social security number for " + student.getFullName() + " to: " + socialSecurityNumber);

                    } else {
                        log.warn("Invalid social security number for " + student.getFullName() + ".");
                    }
                }

                // Update address
                if (record.isMapped("Adresse")) {
                    try {
                        String streetAddress = record.get("Adresse");
                        if (streetAddress != null && streetAddress.length() > 0) {
                            if (record.isMapped("Postnummer") && record.isMapped("Sted")) {
                                String zipInput = record.get("Postnummer");
                                if (zipInput != null && zipInput.length() > 0) {
                                    String cityName = record.get("Sted").replaceAll("[^A-Za-z]", "");
                                    Integer zipCode = Integer.parseInt(zipInput.replaceAll("[^0-9]", ""));
                                    homeAddressService.updateHomeAddress(student, streetAddress, zipCode, cityName);
                                }

                            } else if (record.isMapped("Poststed")) {
                                String zipInput = record.get("Poststed");
                                if (zipInput != null && zipInput.length() > 0) {
                                    String cityName = zipInput.replaceAll("[^A-Za-z]", "");
                                    Integer zipCode = Integer.parseInt(zipInput.replaceAll("[^0-9]", ""));
                                    homeAddressService.updateHomeAddress(student, streetAddress, zipCode, cityName);
                                }
                            }
                        }

                    } catch (Exception ex) {
                        if (student.getHomeAddress() == null) {
                            log.warn("Invalid home address for " + firstName + " " + lastName + ".");
                        }
                    }
                }

                // Other status
                Boolean uptake = null;
                if (record.isMapped("Opptak") && courseType.getUseUptake() != null && courseType.getUseUptake()) {
                    uptake = record.get("Opptak").equalsIgnoreCase("True");
                }

                // Create course application
                courseService.createCourseApplication(student, courseType, uptake);
                students.add(student);
            }
        }

        log.info("Proccessed " + students.size() + " students.");
        return new HashSet<>(students);
    }

}