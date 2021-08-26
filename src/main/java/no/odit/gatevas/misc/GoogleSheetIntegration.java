package no.odit.gatevas.misc;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.service.HomeAddressService;
import no.odit.gatevas.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class GoogleSheetIntegration {

    @Autowired
    private StudentService studentService;

    @Autowired
    private HomeAddressService homeAddressService;

    @Autowired
    private Sheets sheetService;

    @Autowired
    private CourseService courseService;

    @SneakyThrows
//    @PostConstruct
    public void test() {
        List<Request> requestList = new ArrayList<>();

        CellFormat cellFormat = new CellFormat(); //setting cell color
        Color color = new Color();
        color.setRed(246f);
        color.setGreen(178f);
        color.setBlue(107f);

        cellFormat.setBackgroundColor(color);

        CellData cellData = new CellData();
        cellData.setUserEnteredFormat(cellFormat);

        GridRange gridRange = new GridRange(); //setting grid that we will paint
        gridRange.setSheetId(0); //you can find it in your URL - param "gid"
        gridRange.setStartRowIndex(0);
        gridRange.setEndRowIndex(1);
        gridRange.setStartColumnIndex(0);
        gridRange.setEndColumnIndex(14);

        requestList.add(new Request().setRepeatCell(new RepeatCellRequest().setCell(cellData).setRange(gridRange).setFields("userEnteredFormat.backgroundColor")));

        BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateSpreadsheetRequest.setRequests(requestList);

        final Sheets.Spreadsheets.BatchUpdate batchUpdate = sheetService.
                spreadsheets().batchUpdate("1AA6jGM8nsXVDMEEsm7z-IOV51-QnHWBQtKjLXLw00VU", batchUpdateSpreadsheetRequest);

        batchUpdate.execute();
    }

    public Set<Student> processSheet(String spreadSheetId, CourseType courseType) throws IOException {

        long spreadSheetCount = courseService.getCourseTypes().stream()
                .filter(type -> type.getGoogleSheetId() != null && type.getGoogleSheetId().equalsIgnoreCase(courseType.getGoogleSheetId()))
                .count();
        ValueRange response = sheetService.spreadsheets().values()
                .get(spreadSheetId, "A:Z")
                .execute();

        // Output list
        Set<Student> students = new HashSet<Student>();

        // Online sheet data
        List<List<Object>> values = response.getValues();

        // Scan and collect
        if (values == null || values.isEmpty()) {
            log.warn("No data found in spreadsheet.");
        } else {
            HashMap<String, Integer> header = new HashMap<String, Integer>();
            for (List<Object> rawRow : values) {
                List<String> row = Lists.transform(rawRow, Functions.toStringFunction());

                // Load header
                if (header.isEmpty()) {
                    int i = 0;
                    for (String raw : row) {
                        raw = raw.toLowerCase();
                        if ((raw.startsWith("e-postadresse") || raw.startsWith("mail") || raw.startsWith("e-post") || raw.startsWith("epost"))
                                && !raw.contains("faktura"))
                            raw = "email";
                        else if (raw.startsWith("tlf") || raw.startsWith("telefon") || raw.startsWith("mobil"))
                            raw = "phone";
                        else if (raw.startsWith("fornavn"))
                            raw = "first_name";
                        else if (raw.startsWith("etternavn"))
                            raw = "last_name";
                        else if (raw.startsWith("jeg melder meg p"))
                            raw = "course_type";
                        else if (raw.startsWith("fødselsdato"))
                            raw = "birth_date";
                        else if (raw.startsWith("adresse"))
                            raw = "street_address";
                        else if (raw.startsWith("postnr og sted"))
                            raw = "city_zipcode";
                        header.put(raw, i);
                        i++;
                    }

                }
                // Process rows
                else {

                    // Update student information
                    String firstName = row.get(header.get("first_name"));
                    String lastName = row.get(header.get("last_name"));
                    String email = row.get(header.get("email"));
                    String phoneInput = row.get(header.get("phone"));
                    Integer phoneNum = null;
                    try {
                        phoneNum = Integer.parseInt(phoneInput.replace("+47", "").replace(" ", ""));
                    } catch (Exception ex) {
                        log.warn("Invalid phone number '" + phoneInput + "' for " + firstName + " " + lastName + ".");
                    }
                    Student student = studentService.createStudent(email, firstName, lastName, phoneNum);

                    // Update birth date
                    String birthDay = null;
                    try {
                        birthDay = row.get(header.get("birth_date"));
                        birthDay = birthDay.replaceAll("[^A-Za-z0-9]", ".").replace("..", ".");
                        if (birthDay.endsWith(".")) birthDay = birthDay.substring(0, birthDay.length() - 2);
                        if (birthDay.length() <= 8) {
                            String[] split = birthDay.split("\\.");
                            birthDay = split[0] + "." + split[1] + ".19" + split[2];
                        }
                        LocalDate birthDate = LocalDate.parse(birthDay, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                        if (!birthDate.isBefore(LocalDate.now().minusYears(90)) && !birthDate.isAfter(LocalDate.now().minusYears(15))) {
                            student.setBirthDate(birthDate);
                            studentService.saveChanges(student);
                        }
                    } catch (Exception ex) {
                        log.warn("Invalid birth date '" + birthDay + "' for " + firstName + " " + lastName + ".");
                    }

                    // Update home address
                    try {
                        String streetAddress = row.get(header.get("street_address"));
                        String cityZipCode = row.get(header.get("city_zipcode"));
                        String cityName = cityZipCode.replaceAll("[^A-Za-z]", "");
                        Integer zipCode = Integer.parseInt(cityZipCode.replaceAll("[^\\d.]", ""));
                        homeAddressService.updateHomeAddress(student, streetAddress, zipCode, cityName);
                    } catch (Exception ex) {
                        log.warn("Invalid home address for " + firstName + " " + lastName + ".");
                    }

                    // Update course applications
                    if (courseType != null) {
                        if (spreadSheetCount == 1) {
                            courseService.createCourseApplication(student, courseType);
                        } else {
                            log.warn("Duplicate check for spreadsheet in '" + courseType.getShortName() + "' failed.");
                        }
                    }

                    // Add student to output
                    students.add(student);
                }
            }
        }

        // Return list
        return students;
    }
}