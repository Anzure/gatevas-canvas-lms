package no.odit.gatevas.command;

import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.dao.CourseApplicationRepo;
import no.odit.gatevas.dao.CourseRepo;
import no.odit.gatevas.dao.HomeAddressRepo;
import no.odit.gatevas.misc.SheetImportCSV;
import no.odit.gatevas.model.*;
import no.odit.gatevas.service.CourseService;
import no.odit.gatevas.type.ApplicationStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GlobalCommand implements CommandHandler {

    @Value("${gatevas.global.export_path}")
    private String globalExportPath;

    @Value("${gatevas.global.import_path}")
    private String globalImportPath;

    @Autowired
    private CourseService courseService;

    @Autowired
    private Scanner commandScanner;

    @Autowired
    private SheetImportCSV sheetImportCSV;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private CourseApplicationRepo courseApplicationRepo;

    @Autowired
    private HomeAddressRepo homeAddressRepo;

    @Autowired
    private StrongTextEncryptor textEncryptor;

    public void handleCommand(Command cmd) {
        String[] args = cmd.getArgs();

        if (args.length != 1) {
            System.out.println("Available commands:");
            System.out.println("- global import");
            System.out.println("- global export");
            System.out.println("- global custom-export");
            System.out.println("- global course-export");
            System.out.println("- global type-export");
            return;
        }

        // Import student applications from Google Sheets
        if (args[0].equalsIgnoreCase("import")) {

            System.out.println("Import student applications to system.");

            Map<CourseType, String> csvFiles = new HashMap<>();
            for (CourseType type : courseService.getCourseTypes()) {
                if (type.getCsvFile() != null && !type.getCsvFile().equalsIgnoreCase("null")) {
                    csvFiles.put(type, type.getCsvFile());
                }
            }

            System.out.println("Found " + csvFiles.values().stream().collect(Collectors.toSet()).size()
                    + " sheets to process.");
            System.out.print("Want to continue? (Y/N): ");
            if (commandScanner.nextLine().equalsIgnoreCase("Y")) {
                System.out.println("Importing global student list...");
                csvFiles.entrySet().stream().forEach(entry -> {
                    CourseType courseType = entry.getKey();
                    File csvFile = new File(globalImportPath, entry.getValue());
                    try {
                        Set<Student> students = sheetImportCSV.processSheet(csvFile, courseType, "UTF-8", true);
                        System.out.println("Processed " + students.size() + " students in type " + courseType.getShortName() + ".");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Failed to process sheet!");
                    }
                });
                System.out.println("Successfully imported student list.");
            }
        }

        // Export students applications to CSV file
        else if (args[0].equalsIgnoreCase("export")) {

            System.out.println("Export student applications to file.");

            System.out.print("Status (all/waitlist): ");
            String statusInput = commandScanner.nextLine().toUpperCase();
            ApplicationStatus status = null;
            if (!statusInput.equalsIgnoreCase("all")) status = ApplicationStatus.valueOf(statusInput);

            List<CourseApplication> applications;
            if (status == null) applications = courseApplicationRepo.findAll();
            else applications = courseApplicationRepo.findByStatus(status);
            applications = applications.stream()
                    .filter(apply -> apply.getCourse().getCsvFile() != null)
                    .filter(apply -> !apply.getCourse().getCsvFile().equalsIgnoreCase("null"))
                    .sorted(Comparator.comparing(CourseApplication::getCreatedAt))
                    .sorted(Comparator.comparing(e -> e.getCourse().getShortName()))
                    .collect(Collectors.toList());

            String typeName = status != null ? status.toString().toLowerCase() : "all";
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HHmmss");
            String date = dateFormat.format(new Date());
            File file = new File(globalExportPath + File.separator + typeName + "-" + date + ".csv");

            System.out.println("Exporting global student list...");
            try (FileWriter out = new FileWriter(file)) {
                out.write('\ufeff');

                String[] header = {"Utdanning", "Fornavn", "Etternavn", "Fødselsdato", "E-postadresse", "Mobilnummer", "Kurskode", "Dato", "Status", "Opptak"};
                CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

                for (CourseApplication apply : applications) {
                    Student student = apply.getStudent();
                    Phone phone = student.getPhone();
                    LocalDate birthDate = student.getBirthDate();
                    String formattedBirthDate = birthDate != null ? birthDate.format(DateTimeFormatter.ofPattern("ddMMyy")) : "mangler data";
                    if (apply.getStatus() == ApplicationStatus.WAITLIST && student.getEnrollments() != null) {
                        boolean alreadyEnrolled = student.getEnrollments().stream()
                                .map(enroll -> enroll.getCourse().getType())
                                .filter(type -> apply.getCourse().equals(type))
                                .findAny().isPresent();
                        if (alreadyEnrolled) {
                            apply.setStatus(ApplicationStatus.ACCEPTED);
                            courseApplicationRepo.saveAndFlush(apply);
                            System.out.println("Fixed status for '" + student.getFullName() + "' in " + apply.getCourse().getShortName() + ".");
                        }
                    }
                    printer.printRecord(apply.getCourse().getLongName(),
                            student.getFirstName(),
                            student.getLastName(),
                            formattedBirthDate,
                            student.getEmail(),
                            phone != null ? phone.getPhoneNumber() : 0,
                            apply.getCourse().getShortName(),
                            apply.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            apply.getStatus().toString(),
                            apply.getUptake() == null ? "Ukjent" : apply.getUptake() ? "Ja" : "Nei");
                }
                printer.close();
                System.out.println("Successfully exported student list.");

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Failed to export global list.");
            }
        }

        // Export students applications to CSV file
        else if (args[0].equalsIgnoreCase("custom-export")) {

            System.out.println("Export student applications to file.");

            LocalDateTime afterTime = LocalDateTime.of(2021, 2, 1, 0, 0);
            LocalDateTime beforeTime = LocalDateTime.now().minusWeeks(5);
            List<Classroom> courses = courseRepo.findAll().stream()
                    .filter(course -> course.getCreatedAt().isAfter(afterTime))
                    .filter(course -> course.getCreatedAt().isBefore(beforeTime))
                    .collect(Collectors.toList());

            String typeName = "custom";
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HHmmss");
            String date = dateFormat.format(new Date());
            File file = new File(globalExportPath + File.separator + typeName + "-" + date + ".csv");

            List<String> temp = new ArrayList<>();

            System.out.println("Exporting global student list...");
            try (FileWriter out = new FileWriter(file)) {
                out.write('\ufeff');

                String[] header = {"E-postadresse", "Kursnavn", "Fornavn", "Etternavn", "Tlf nr", "Kurskode", "Dato", "Status"};
                CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

                for (Classroom course : courses) {
                    for (RoomLink enrollment : course.getEnrollments()) {
                        Student student = enrollment.getStudent();

                        if (enrollment.getCreatedAt().isAfter(beforeTime)) {
                            continue;
                        }

                        CourseApplication apply = courseApplicationRepo.findByStudentAndCourse(student, course.getType())
                                .orElse(null);
                        if (apply == null || apply.getStatus() == ApplicationStatus.WAITLIST
                                || apply.getStatus() == ApplicationStatus.WITHDRAWN) {
                            continue;
                        }

                        if (temp.contains(student.getEmail().toLowerCase())) {
                            continue;
                        } else {
                            temp.add(student.getEmail().toLowerCase());
                        }

                        Phone phone = student.getPhone();
                        printer.printRecord(student.getEmail(),
                                course.getLongName(),
                                student.getFirstName(),
                                student.getLastName(),
                                phone != null ? phone.getPhoneNumber() : 0,
                                course.getShortName(),
                                enrollment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                apply != null ? apply.getStatus().toString() : "UNKNOWN");
                    }
                }
                printer.close();
                System.out.println("Successfully exported student list.");

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Failed to export global list.");
            }
        }

        // Export course student lists to CSV file
        else if (args[0].equalsIgnoreCase("semester-export")) {

            System.out.println("Export course student lists to file.");

            LocalDateTime afterTime = LocalDateTime.of(2021, 10, 1, 0, 0);
            LocalDateTime beforeTime = LocalDateTime.of(2022, 2, 28, 0, 0);
            List<Classroom> courses = courseRepo.findAll().stream()
                    .filter(course -> course.getCreatedAt().isAfter(afterTime))
                    .filter(course -> course.getCreatedAt().isBefore(beforeTime))
                    .collect(Collectors.toList());

            String typeName = "semester";
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HHmmss");
            String date = dateFormat.format(new Date());
            File file = new File(globalExportPath + File.separator + typeName + "-" + date + ".csv");

            try (FileWriter out = new FileWriter(file)) {
                out.write('\ufeff');

                String[] header = {"Kurs", "Fornavn", "Etternavn", "Fødselsnummer", "E-postadresse", "Mobilnummer",
                        "Adresse", "Poststed", "Status"};
                CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

                for (Classroom course : courses) {
                    if (course.getEnrollments().stream().count() <= 0
                            || course.getType().getCsvFile() == null
                            || course.getType().getCsvFile().equalsIgnoreCase("null")) {
                        System.out.println("Ignored course '" + course.getShortName() + "'.");
                        continue;
                    }
                    System.out.println("Processing course '" + course.getShortName() + "'...");

                    for (Student student : course.getStudents()) {
                        CourseApplication apply = courseApplicationRepo.findByStudentAndCourse(student, course.getType()).orElse(null);
                        Phone phone = student.getPhone();
                        LocalDate birthDate = student.getBirthDate();
                        String formattedBirthDate = birthDate != null ? birthDate.format(DateTimeFormatter.ofPattern("ddMMyy")) : "";
                        String formattedPhoneNum = phone != null && phone.getPhoneNumber() != 0 ? String.valueOf(phone.getPhoneNumber()) : "";
                        String applyStatus = apply != null ? apply.getStatus().toString()
                                .replace("ACCEPTED", "Ikke fullført")
                                .replace("WITHDRAWN", "Avmeldt")
                                .replace("FINISHED", "Fullført") : "";
                        HomeAddress homeAddress = homeAddressRepo.findByStudent(student).orElse(null);
                        String address = homeAddress != null ? homeAddress.getStreetAddress() : "";
                        if (homeAddress != null && address.length() <= 2) address = "";
                        String postal = homeAddress != null ? homeAddress.getZipCode() + " " + homeAddress.getCity() : "";
                        String socialSecurityNumber = student.getSocialSecurityNumber() != null ? textEncryptor.decrypt(student.getSocialSecurityNumber()) : formattedBirthDate;

                        printer.printRecord(course.getLongName(),
                                student.getFirstName(),
                                student.getLastName(),
                                socialSecurityNumber,
                                student.getEmail(),
                                formattedPhoneNum,
                                address,
                                postal,
                                applyStatus);
                    }
                    System.out.println("Exported course '" + course.getShortName() + "'!");
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

            LocalDateTime afterTime = LocalDateTime.now().minusMonths(6);
            List<Classroom> courses = courseService.getAllCourses().stream()
                    .filter(course -> course.getCreatedAt().isAfter(afterTime))
                    .collect(Collectors.toList());
            for (Classroom course : courses) {
                if (course.getEnrollments().stream().count() <= 0
                        || course.getType().getCsvFile() == null
                        || course.getType().getCsvFile().equalsIgnoreCase("null")) {
                    System.out.println("Ignored course '" + course.getShortName() + "'.");
                    continue;
                }
                System.out.println("Processing course '" + course.getShortName() + "'...");

                File file = new File(globalExportPath + File.separator + course.getShortName() + "-" + date + ".csv");

                try (FileWriter out = new FileWriter(file)) {
                    out.write('\ufeff');

                    String[] header = {"E-postadresse", "Kurs", "Fornavn", "Etternavn", "Fodselsdato", "Adresse", "Poststed", "Tlf nr", "Status"};
                    CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

                    for (Student student : course.getStudents()) {
                        CourseApplication apply = courseApplicationRepo.findByStudentAndCourse(student, course.getType()).orElse(null);
                        Phone phone = student.getPhone();
                        LocalDate birthDate = student.getBirthDate();
                        String formattedBirthDate = birthDate != null ? birthDate.format(DateTimeFormatter.ofPattern("ddMMyy")) : "mangler data";
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
                    System.out.println("Exported course '" + course.getShortName() + "'!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to export global list.");
                }
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
                    System.out.println("Ignored course type '" + type.getShortName() + "'.");
                    continue;
                }

                System.out.println("Processing course type '" + type.getShortName() + "'...");
                File file = new File(globalExportPath + File.separator + type.getLongName() + " " + date + ".csv");

                try (FileWriter out = new FileWriter(file)) {
                    out.write('\ufeff');

                    String[] header = {"E-postadresse", "Kurs", "Fornavn", "Etternavn", "Fodselsdato", "Adresse", "Poststed", "Tlf nr", "Status"};
                    CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header));

                    for (CourseApplication apply : applications) {
                        Student student = apply.getStudent();
                        Phone phone = student.getPhone();
                        LocalDate birthDate = student.getBirthDate();
                        String formattedBirthDate = birthDate != null ? birthDate.format(DateTimeFormatter.ofPattern("ddMMyy")) : "mangler data";
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
                    System.out.println("Exported course '" + type.getShortName() + "'!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to export global list.");
                }
            }
        }
    }
}