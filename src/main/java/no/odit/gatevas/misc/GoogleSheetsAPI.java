package no.odit.gatevas.misc;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.dao.CourseApplicationRepo;
import no.odit.gatevas.model.CourseApplication;
import no.odit.gatevas.model.CourseType;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.service.HomeAddressService;
import no.odit.gatevas.service.StudentService;
import no.odit.gatevas.type.ApplicationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class GoogleSheetsAPI {

    @Autowired
    private CourseApplicationRepo courseApplicationRepo;

    @Autowired
    private StudentService studentService;

    @Autowired
    private HomeAddressService homeAddressService;

    @Autowired
    private Sheets sheetService;

    @Autowired
    private CourseService courseService;

    @SneakyThrows
    private Request updateRowColor(int sheetId, int rowIndex, int columns, int red, int green, int blue) {
        CellFormat cellFormat = new CellFormat();
        Color color = new Color();
        color.setRed((float) red / 255f);
        color.setGreen((float) green / 255f);
        color.setBlue((float) blue / 255f);
        cellFormat.setBackgroundColor(color);

        CellData cellData = new CellData();
        cellData.setUserEnteredFormat(cellFormat);

        GridRange gridRange = new GridRange();
        gridRange.setSheetId(sheetId);
        gridRange.setStartRowIndex(rowIndex);
        gridRange.setEndRowIndex(rowIndex + 1);
        gridRange.setStartColumnIndex(0);
        gridRange.setEndColumnIndex(columns);

        return new Request().setRepeatCell(new RepeatCellRequest()
                .setCell(cellData)
                .setRange(gridRange)
                .setFields("userEnteredFormat.backgroundColor"));
    }

    @SneakyThrows
    private void updateSheetColors(String spreadSheetId, List<Request> requests) {
        BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateSpreadsheetRequest.setRequests(requests);
        Sheets.Spreadsheets.BatchUpdate batchUpdate = sheetService.spreadsheets().batchUpdate(spreadSheetId, batchUpdateSpreadsheetRequest);
        batchUpdate.execute();
    }

    @SneakyThrows
    public Set<Student> processSheet(String spreadSheetId, CourseType courseType, boolean verifyName) {

        long spreadSheetCount = courseService.getCourseTypes().stream()
                .filter(type -> type.getGoogleSheetId() != null && type.getGoogleSheetId().equalsIgnoreCase(courseType.getGoogleSheetId()))
                .count();
        Spreadsheet spreadsheet = sheetService.spreadsheets()
                .get(spreadSheetId)
                .execute();
        ValueRange response = sheetService.spreadsheets().values()
                .get(spreadSheetId, "A:Z")
                .execute();
        Sheet sheet = spreadsheet.getSheets().get(0);
        SheetProperties props = sheet.getProperties();
        List<Request> colorUpdateRequests = new ArrayList<>();

        // Safefy check
        if (verifyName && !spreadsheet.getProperties().getTitle().toLowerCase().contains(courseType.getLongName().toLowerCase())) {
            log.warn("Spreadsheet title: " + spreadsheet.getProperties().getTitle());
            log.warn("Course type name: " + courseType.getLongName());
            throw new Error("Safety check stopped processing spreadsheet!");
        }

        // Output list
        Set<Student> students = new HashSet<Student>();

        // Online sheet data
        List<List<Object>> values = response.getValues();

        // Check if empty
        if (values == null || values.isEmpty() || values.size() == 0) {
            log.warn("No data found in spreadsheet.");
            return new HashSet<Student>();
        }

        // Scan and collect
        HashMap<String, Integer> header = new HashMap<String, Integer>();
        for (int rowIndex = 0; rowIndex < values.size(); ++rowIndex) {
            List<String> row = Lists.transform(values.get(rowIndex), Functions.toStringFunction());

            // Load header
            if (header.isEmpty()) {
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
                    else if (raw.startsWith("navn"))
                        raw = "full_name";
                    else if (raw.startsWith("jeg melder meg p"))
                        raw = "course_type";
                    else if (raw.startsWith("fødselsdato"))
                        raw = "birth_date";
                    else if (raw.startsWith("adresse"))
                        raw = "street_address";
                    else if (raw.startsWith("postnr og sted"))
                        raw = "city_zipcode";
                    header.put(raw, header.size());
                }
            }

            // Process rows
            else {

                // Update student information
                String firstName = header.containsKey("first_name") ? row.get(header.get("first_name"))
                        : row.get(header.get("full_name")).split(" ")[0];
                String lastName = header.containsKey("last_name") ? row.get(header.get("last_name"))
                        : row.get(header.get("full_name")).replaceFirst(firstName, "");
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
                    if (student.getBirthDate() == null) {
                        log.warn("Invalid birth date '" + birthDay + "' for " + firstName + " " + lastName + ".");
                    }
                }

                // Update home address
                try {
                    String streetAddress = row.get(header.get("street_address"));
                    String cityZipCode = row.get(header.get("city_zipcode"));
                    String cityName = cityZipCode.replaceAll("[^A-Za-z]", "");
                    Integer zipCode = Integer.parseInt(cityZipCode.replaceAll("[^\\d.]", ""));
                    homeAddressService.updateHomeAddress(student, streetAddress, zipCode, cityName);
                } catch (Exception ex) {
                    if (student.getHomeAddress() == null) {
                        log.warn("Invalid home address for " + firstName + " " + lastName + ".");
                    }
                }

                // Update course applications
                if (spreadSheetCount == 1) {
                    courseService.createCourseApplication(student, courseType);
                } else {
                    log.warn("Duplicate check for spreadsheet in '" + courseType.getShortName() + "' failed.");
                }

                // Update row color
                Optional<CourseApplication> apply = courseApplicationRepo.findByStudentAndCourse(student, courseType);
                if (apply.isPresent()) {
                    ApplicationStatus status = apply.get().getStatus();
                    if (status == ApplicationStatus.ACCEPTED || status == ApplicationStatus.FINISHED) {
                        colorUpdateRequests.add(updateRowColor(props.getSheetId(), rowIndex, header.size(), 147, 196, 125));
                    } else if (status == ApplicationStatus.WITHDRAWN || status == ApplicationStatus.FAILED) {
                        colorUpdateRequests.add(updateRowColor(props.getSheetId(), rowIndex, header.size(), 224, 102, 102));
                    } else {
                        colorUpdateRequests.add(updateRowColor(props.getSheetId(), rowIndex, header.size(), 255, 217, 102));
                    }
                }

                // Add student to output
                students.add(student);
            }
        }

        // Update sheet colors
        if (colorUpdateRequests.size() > 0) {
            updateSheetColors(spreadSheetId, colorUpdateRequests);
        }

        // Return list
        return students;
    }

}