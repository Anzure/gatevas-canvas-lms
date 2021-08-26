package no.odit.gatevas.misc;

import no.odit.gatevas.model.Student;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class ExcelSheetsCSV {

    // Creates user import CSV file in Canvas SIS format
    public void createCSVFile(File outputFile, List<Student> students) throws IOException {
        String[] headers = {"user_id", "password", "first_name", "last_name", "email", "login_id", "status"};
        FileWriter out = new FileWriter(outputFile);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers))) {
            for (Student student : students) {
                printer.printRecord(student.getUserId(), student.getTmpPassword(), student.getFirstName(),
                        student.getLastName(), student.getEmail(), student.getEmail(), "active");
            }
        }
    }
}