package no.odit.gatevas.misc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.service.StudentService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
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
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @SneakyThrows
    public Set<Student> processSheet(File csvFile, CourseType courseType) {

        List<Student> students = new ArrayList<>();

        log.info("Proccessing " + csvFile.getName() + " spreadsheet...");

        FileReader reader = new FileReader(csvFile);
        try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(','))) {

            List<CSVRecord> records = parser.getRecords();
            log.debug("Found " + records.size() + " records.");
            for (CSVRecord record : records) {
                String education = record.get("Utdanning");
                if (courseType == null) courseType = courseService.getCourseType(education).orElseThrow();
                else if (!education.equalsIgnoreCase(courseType.getLongName())
                        && !education.equalsIgnoreCase(courseType.getShortName())
                        && !education.equalsIgnoreCase(courseType.getAliasName())) continue;

                String firstName = record.get("Fornavn");
                String lastName = record.get("Etternavn");

                String emailAddress = record.get("E-postadresse");

                String phoneInput = record.get("Mobilnummer");
                phoneInput = phoneInput.replaceAll("[^0-9]", "");
                phoneInput = phoneInput.length() > 8 ? phoneInput.substring(phoneInput.length() - 8) : phoneInput;
                Integer phoneNumber = phoneInput.length() > 0 ? Integer.parseInt(phoneInput) : null;

                String birthInput = record.get("Fødselsdato");
                LocalDate birthDate = LocalDate.parse(birthInput, DateTimeFormatter.ofPattern("ddMMyy"));

                Student student = studentService.createStudent(emailAddress, firstName, lastName, phoneNumber, birthDate);

                courseService.createCourseApplication(student, courseType);
                students.add(student);
            }
        }

        log.info("Proccessed " + students.size() + " students.");
        return new HashSet<>(students);
    }

}