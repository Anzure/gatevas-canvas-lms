package no.odit.gatevas.command;

import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.dao.CourseApplicationRepo;
import no.odit.gatevas.dao.HomeAddressRepo;
import no.odit.gatevas.misc.GoogleSheetsAPI;
import no.odit.gatevas.model.*;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.type.ApplicationStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GlobalCommand implements CommandHandler {

    @Value("${gatevas.global.export_path}")
    private String globalExportPath;

    @Autowired
    private CourseService courseService;

    @Autowired
    private Scanner commandScanner;

    @Autowired
    private GoogleSheetsAPI googleSheetsAPI;

    @Autowired
    private CourseApplicationRepo courseApplicationRepo;

    @Autowired
    private HomeAddressRepo homeAddressRepo;

    public void handleCommand(Command cmd) {
        String[] args = cmd.getArgs();

        if (args.length != 1) {
            System.out.println("Available commands:");
            System.out.println("- global import");
            System.out.println("- global export");
            System.out.println("- global course-export");
            return;
        }

        // Import student applications from Google Sheets
        if (args[0].equalsIgnoreCase("import")) {

            System.out.println("Import student applications to system.");

            Map<String, CourseType> globalSheets = new HashMap<>();
            for (CourseType type : courseService.getCourseTypes()) {
                if (type.getGoogleSheetId() != null && !type.getGoogleSheetId().equalsIgnoreCase("null")) {
                    globalSheets.put(type.getGoogleSheetId(), type);
                }
            }

            System.out.println("Found " + globalSheets.size() + " sheets to process.");
            System.out.print("Want to continue? (Y/N): ");
            if (commandScanner.nextLine().equalsIgnoreCase("Y")) {
                globalSheets.entrySet().stream().forEach(entry -> {
                    CourseType courseType = entry.getValue();
                    String googleSpreadSheetId = entry.getKey();
                    try {
                        Set<Student> students = googleSheetsAPI.processSheet(googleSpreadSheetId, courseType);
                        System.out.println("Successfully processed " + students.size() + " students.");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Failed to process sheet!");
                    }
                });
            }
        }

        // Export students applications to CSV file
        else if (args[0].equalsIgnoreCase("export")) {

            System.out.println("Export student applications to file.");

            System.out.print("Status (all/waitlist): ");
            String statusInput = commandScanner.nextLine().toUpperCase();
            ApplicationStatus status = null;
            if (!statusInput.equalsIgnoreCase("all")) status = ApplicationStatus.valueOf(statusInput);

            List<CourseApplication> applications = null;
            if (status == null) applications = courseApplicationRepo.findAll();
            else applications = courseApplicationRepo.findByStatus(status);
            applications = applications.stream()
                    .sorted(Comparator.comparing(CourseApplication::getCreatedAt))
                    .sorted(Comparator.comparing(e -> e.getCourse().getShortName()))
                    .collect(Collectors.toList());

            String typeName = status != null ? status.toString().toLowerCase() : "all";
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HHmmss");
            String date = dateFormat.format(new Date());
            File file = new File(globalExportPath + File.separator + typeName + "-" + date + ".csv");

            try (FileWriter out = new FileWriter(file)) {
                out.write('\ufeff');

                String[] header = {"E-postadresse", "Kursnavn", "Fornavn", "Etternavn", "Tlf nr", "Kurskode", "Dato", "Status"};
                CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

                for (CourseApplication apply : applications) {
                    Student student = apply.getStudent();
                    Phone phone = student.getPhone();
                    printer.printRecord(student.getEmail(),
                            apply.getCourse().getLongName(),
                            student.getFirstName(),
                            student.getLastName(),
                            phone != null ? phone.getPhoneNumber() : 0,
                            apply.getCourse().getShortName(),
                            apply.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            apply.getStatus().toString());
                }

                printer.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Failed to export global list.");
            }
        }

        // Export course student lists to CSV file
        else if (args[0].equalsIgnoreCase("course-export")) {

            System.out.println("Export course student lists to file.");

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HHmmss");
            String date = dateFormat.format(new Date());

            List<Classroom> courses = courseService.getAllCourses();
            for (Classroom course : courses) {
                if (course.getEnrollments().stream().count() <= 0
                        || course.getType().getGoogleSheetId() == null
                        || course.getType().getGoogleSheetId().equalsIgnoreCase("null")) {
                    log.debug("Ignored course '" + course.getShortName() + "'.");
                    continue;
                }
                log.debug("Processing course '" + course.getShortName() + "'...");

                File file = new File(globalExportPath + File.separator + course.getShortName() + "-" + date + ".csv");

                try (FileWriter out = new FileWriter(file)) {
                    out.write('\ufeff');

                    String[] header = {"E-postadresse", "Kurs", "Fornavn", "Etternavn", "Fodselsdato", "Adresse", "Poststed", "Tlf nr", "Status"};
                    CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

                    for (Student student : course.getStudents()) {
                        CourseApplication apply = courseApplicationRepo.findByStudentAndCourse(student, course.getType()).orElse(null);
                        Phone phone = student.getPhone();
                        LocalDate birthDate = student.getBirthDate();
                        String formattedBirthDate = birthDate != null ? birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "mangler data";
                        String formattedPhoneNum = phone != null && phone.getPhoneNumber() != 0 ? String.valueOf(phone.getPhoneNumber()) : "mangler data";
                        String applyStatus = apply != null ? apply.getStatus().toString()
                                .replace("ACCEPTED", "Ikke fullført")
                                .replace("WITHDRAWN", "Avmeldt")
                                .replace("FINISHED", "Fullført") : "mangler data";
                        HomeAddress homeAddress = homeAddressRepo.findByStudent(student).orElse(null);
                        String address = homeAddress != null ? homeAddress.getStreetAddress() : "mangler data";
                        if (homeAddress != null && address.length() <= 2) address = "mangler data";
                        String postal = homeAddress != null ? homeAddress.getZipCode() + " " + homeAddress.getCity() : "mangler data";
                        printer.printRecord(student.getEmail(),
                                course.getLongName(),
                                student.getFirstName(),
                                student.getLastName(),
                                formattedBirthDate,
                                address,
                                postal,
                                formattedPhoneNum,
                                applyStatus);
                    }

                    printer.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to export global list.");
                }

                log.debug("Exported course '" + course.getShortName() + "'!");
            }
        }

        // Export course student lists to CSV file
        else if (args[0].equalsIgnoreCase("type-export")) {

            System.out.println("Export course student lists to file.");

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.YY");
            String date = dateFormat.format(new Date());

            List<CourseType> courseTypes = courseService.getCourseTypes();
            for (CourseType type : courseTypes) {

                List<CourseApplication> applications = type.getApplications().stream()
                        .sorted(Comparator.comparing(CourseApplication::getCreatedAt))
                        .collect(Collectors.toList());
                if (applications.size() <= 0) {
                    log.debug("Ignored course type '" + type.getShortName() + "'.");
                    continue;
                }

                log.debug("Processing course type '" + type.getShortName() + "'...");
                File file = new File(globalExportPath + File.separator + type.getLongName() + " " + date + ".csv");

                try (FileWriter out = new FileWriter(file)) {
                    out.write('\ufeff');

                    String[] header = {"E-postadresse", "Kurs", "Fornavn", "Etternavn", "Fodselsdato", "Adresse", "Poststed", "Tlf nr", "Status"};
                    CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

                    for (CourseApplication apply : applications) {
                        Student student = apply.getStudent();
                        Phone phone = student.getPhone();
                        LocalDate birthDate = student.getBirthDate();
                        String formattedBirthDate = birthDate != null ? birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "mangler data";
                        String formattedPhoneNum = phone != null && phone.getPhoneNumber() != 0 ? String.valueOf(phone.getPhoneNumber()) : "mangler data";
                        String applyStatus = apply != null ? apply.getStatus().toString()
                                .replace("ACCEPTED", "Tilbudt plass")
                                .replace("WITHDRAWN", "Avmeldt")
                                .replace("WAITLIST", "Venteliste")
                                .replace("FINISHED", "Tilbudt plass") : "Venteliste";
                        HomeAddress homeAddress = homeAddressRepo.findByStudent(student).orElse(null);
                        String address = homeAddress != null ? homeAddress.getStreetAddress() : "mangler data";
                        if (homeAddress != null && address.length() <= 2) address = "mangler data";
                        String postal = homeAddress != null ? homeAddress.getZipCode() + " " + homeAddress.getCity() : "mangler data";
                        printer.printRecord(student.getEmail(),
                                type.getLongName(),
                                student.getFirstName(),
                                student.getLastName(),
                                formattedBirthDate,
                                address,
                                postal,
                                formattedPhoneNum,
                                applyStatus);
                    }
                    printer.close();

                    log.debug("Exported course '" + type.getShortName() + "'!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to export global list.");
                }
            }
        }
    }
}