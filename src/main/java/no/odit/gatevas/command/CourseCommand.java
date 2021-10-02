package no.odit.gatevas.command;

import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.cli.Command;
import no.odit.gatevas.cli.CommandHandler;
import no.odit.gatevas.dao.CourseApplicationRepo;
import no.odit.gatevas.dao.HomeAddressRepo;
import no.odit.gatevas.model.*;
import no.odit.gatevas.service.*;
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
import java.util.Date;
import java.util.List;
import java.util.Scanner;

@Component
@Slf4j
public class CourseCommand implements CommandHandler {

    @Value("${gatevas.global.export_path}")
    private String globalExportPath;

    @Value("${gatevas.course.export_path}")
    private String courseExportPath;

    @Autowired
    private HomeAddressRepo homeAddressRepo;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private Scanner commandScanner;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CanvasService canvasService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PhoneService phoneService;

    @Autowired
    private CourseApplicationRepo courseApplicationRepo;

    public void handleCommand(Command cmd) {
        String[] args = cmd.getArgs();

        if (args.length != 1) {
            System.out.println("Available commands:");
            System.out.println("- course list");
            System.out.println("- course add");
            System.out.println("- course remove");
            System.out.println("- course info");
            System.out.println("- course import");
            System.out.println("- course export");
            System.out.println("- course enroll");
            System.out.println("- course sync");
            System.out.println("- course email");
            System.out.println("- course sms");
            System.out.println("- course exam");
            System.out.println("- course overview");
            return;
        }

        // View list of courses
        if (args[0].equalsIgnoreCase("list")) {

            List<Classroom> courses = courseService.getAllCourses();
            System.out.println("Course list (" + courses.size() + "):");
            courses.forEach(course -> System.out.println("- " + course.getShortName() + " | " + course.getLongName()));
        }

        // Add a new course
        else if (args[0].equalsIgnoreCase("add")) {

            System.out.println("Create a new course.");
            Classroom course = new Classroom();

            System.out.print("Enter course type: ");
            CourseType type = courseService.getCourseType(commandScanner.nextLine()).orElse(null);
            course.setType(type);

            System.out.print("Enter course period: ");
            course.setPeriod(commandScanner.nextLine());

            System.out.print("Enter google sheet id: ");
            course.setGoogleSheetId(commandScanner.nextLine());

            System.out.print("Shall social group be used? (Y/N): ");
            if (commandScanner.nextLine().equalsIgnoreCase("Y")) {
                System.out.print("Enter social group link: ");
                course.setSocialGroup(commandScanner.nextLine());
            }

            System.out.println("Creating course '" + course.getShortName() + "'...");
            course = courseService.addCourse(course);
            System.out.println("Course created with ID " + course.getId().toString() + ".");
        }

        // Remove a course
        else if (args[0].equalsIgnoreCase("remove")) {

            System.out.println("Remove an existing course.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {
                System.out.println("Deleting '" + course.getShortName() + "'...");
                courseService.removeCourse(course);
                System.out.println("Deleted '" + courseName + "'.");
            }, () -> {
                System.out.println("Could not find course '" + courseName + "'!");
            });
        }

        // Get information about course
        else if (args[0].equalsIgnoreCase("info")) {

            System.out.println("Retrieve course details.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {
                System.out.println("Details about course '" + course.getShortName() + "':");
                System.out.println(course.toString());
            }, () -> {
                System.out.println("Could not find course '" + courseName + "'!");
            });
        }

        // Import students from Google Sheets
        else if (args[0].equalsIgnoreCase("import")) {

            System.out.println("Import students to course.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {

                System.out.println("Importing students from Google Spreadsheets...");
                courseService.importStudents(course).ifPresentOrElse(students -> {

                    System.out.println("Imported " + students.size() + " students to '" + course.getShortName() + "'.");
                    System.out.println("Enrolling " + students.size() + " students to " + course.getShortName() + "...");
                    List<RoomLink> enrollments = enrollmentService.enrollStudent(students, course);
                    System.out.println("Enrolled " + enrollments.size() + " students to '" + course.getShortName() + "'.");

                }, () -> {
                    System.out.println("Failed to import students to '" + course.getShortName() + "'.");
                });

            }, () -> {
                System.out.println("Could not find course '" + courseName + "'!");
            });
        }

        // Export missing students to CSV file
        else if (args[0].equalsIgnoreCase("export")) {

            System.out.println("Export students in course.");

            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HHmmss");
                String date = dateFormat.format(new Date());
                File file = new File(courseExportPath + File.separator + course.getShortName() + "_" + date + ".csv");

                if (studentService.exportStudentsToCSV(course, file))
                    System.out.println("User CSV file created.");
                else
                    System.out.println("Failed to create CSV file.");

            }, () -> {
                System.out.println("Could not find course '" + courseName + "'!");
            });

        }

        // Synchronize course with student enrollments
        else if (args[0].equalsIgnoreCase("sync")) {

            System.out.println("Synchronize Canvas LMS course with local data.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {

                if (canvasService.syncCourseReadOnly(course)) {
                    System.out.println("Canvas LMS course synchronized with local data.");
                    canvasService.syncUsersReadOnly(course);
                    System.out.println("Canvas LMS course students synchronized with local data.");
                } else {
                    System.out.println("Failed to synchronize course.");
                }

            }, () -> {
                System.out.println("Could not find course '" + courseName + "'!");
            });

        }

        // Add students the course in Canvas LMS
        else if (args[0].equalsIgnoreCase("enroll")) {

            System.out.println("Enroll students to course.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {

                if (canvasService.enrollStudents(course))
                    System.out.println("Enrolled students in '" + course.getShortName() + "'.");
                else
                    System.out.println("Failed to enroll students to '" + course.getShortName() + "'.");

            }, () -> {
                System.out.println("Could not find course '" + courseName + "'!");
            });
        }

        // Send email to students about the course and login information
        else if (args[0].equalsIgnoreCase("email")) {

            System.out.println("Send email to students.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {

                if (!course.getStudents().isEmpty()) {
                    System.out.println("Test email sent.");
                    Student testStudent = course.getStudents().get(0);
                    emailService.sendEmail(course, testStudent, true);

                    System.out.print("Want to continue? (Y/N): ");
                    if (commandScanner.nextLine().equalsIgnoreCase("Y")) {
                        emailService.sendEmail(course);
                    }
                }

            }, () -> {
                System.out.println("Could not find course '" + courseName + "'!");
            });
        }

        // Send SMS to students about the course and login information
        else if (args[0].equalsIgnoreCase("sms")) {

            System.out.println("Send sms to students.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {

                if (!course.getStudents().isEmpty()) {
                    System.out.println("Test sms sent.");
                    Student testStudent = course.getStudents().get(0);
                    phoneService.sendSMS(course, testStudent, true);

                    System.out.print("Want to continue? (Y/N): ");
                    if (commandScanner.nextLine().equalsIgnoreCase("Y")) {
                        phoneService.sendSMS(course);
                    }
                }

            }, () -> {
                System.out.println("Could not find course '" + courseName + "'!");
            });
        }

        // Change student status in course
        else if (args[0].equalsIgnoreCase("exam")) {

            System.out.println("Change student status.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {

                //				System.out.print("Enter status (failed/finished): ");
                //				ApplicationStatus status = ApplicationStatus.valueOf(commandScanner.nextLine().toUpperCase());
                ApplicationStatus status = ApplicationStatus.FINISHED;

                System.out.println("Enter student list:");
                while (true) {
                    String textInput = commandScanner.nextLine();
                    if (textInput == null || textInput.equalsIgnoreCase("STOP") || textInput.equalsIgnoreCase("EXIT")) {
                        break;
                    }
                    studentService.getUserByFullName(textInput).ifPresentOrElse(student -> {
                        CourseApplication application = courseApplicationRepo
                                .findByStudentAndCourse(student, course.getType()).orElse(null);
                        application.setStatus(status);
                        courseApplicationRepo.saveAndFlush(application);
                        log.info("Updated " + textInput + "' to " + status.toString() + " in '" + courseName + "'.");
                    }, () -> {
                        log.warn("Could not find '" + textInput + "'!");
                    });
                }

            }, () -> {
                log.error("Could not find course '" + courseName + "'!");
            });

        }

        // Reset exam status for students in course
        else if (args[0].equalsIgnoreCase("reset-exam")) {

            System.out.println("Change student status.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {

                course.getStudents().forEach(student -> {
                    CourseApplication application = courseApplicationRepo
                            .findByStudentAndCourse(student, course.getType()).orElse(null);
                    if (application.getStatus() == ApplicationStatus.FINISHED) {
                        application.setStatus(ApplicationStatus.ACCEPTED);
                        courseApplicationRepo.saveAndFlush(application);
                        log.info("Reset exam status for '" + student.getFullName() + "' in " + course.getShortName() + ".");
                    }
                });

            }, () -> {
                log.error("Could not find course '" + courseName + "'!");
            });
        }

        // Export course student lists to CSV file
        else if (args[0].equalsIgnoreCase("overview")) {

            System.out.println("Export course student lists to file.");
            System.out.print("Enter course name: ");
            String courseName = commandScanner.nextLine();

            courseService.getCourse(courseName).ifPresentOrElse((course) -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HHmmss");
                String date = dateFormat.format(new Date());

                log.info("Processing course '" + course.getShortName() + "'...");

                File file = new File(globalExportPath + File.separator + course.getShortName() + "-" + date + ".csv");
                try (FileWriter out = new FileWriter(file)) {
                    out.write('\ufeff');

                    String[] header = {"E-postadresse", "Kurs", "Fornavn", "Etternavn", "Fodselsdato", "Adresse", "Poststed", "Tlf nr", "Status"};
                    try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(';').withHeader(header))) {
                        for (Student student : course.getStudents()) {
                            CourseApplication apply = courseApplicationRepo.findByStudentAndCourse(student, course.getType()).orElse(null);
                            Phone phone = student.getPhone();
                            LocalDate birthDate = student.getBirthDate();
                            String formattedBirthDate = birthDate != null ? birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "mangler data";
                            String formattedPhoneNum = phone != null && phone.getPhoneNumber() != 0 ? String.valueOf(phone.getPhoneNumber()) : "mangler data";
                            String applyStatus = apply != null ? apply.getStatus().toString()
                                    .replace("ACCEPTED", "Ikke fullført")
                                    .replace("WITHDRAWN", "Avmeldt")
                                    .replace("WAITLIST", "Ukjent")
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
                    }
                    log.info("Exported course '" + course.getShortName() + "'!");

                } catch (Exception ex) {
                    log.error("Failed to export course list.", ex);
                }

            }, () -> {
                log.error("Could not find course '" + courseName + "'!");
            });
        }

    }
}