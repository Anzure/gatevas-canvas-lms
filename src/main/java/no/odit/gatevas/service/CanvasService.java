package no.odit.gatevas.service;

import edu.ksu.canvas.CanvasApiFactory;
import edu.ksu.canvas.interfaces.*;
import edu.ksu.canvas.model.Account;
import edu.ksu.canvas.model.Enrollment;
import edu.ksu.canvas.model.Role;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.model.assignment.Assignment;
import edu.ksu.canvas.model.assignment.Submission;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.*;
import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.misc.CanvasAPI;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.CourseApplication;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.ApplicationStatus;
import no.odit.gatevas.type.CanvasStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CanvasService {

    @Autowired
    private CanvasAPI canvasAPI;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private EnrollmentService enrollmentService;

    // Add students to course in Canvas LMS.
    public boolean enrollStudents(Classroom classRoom) {
        try {

            // Authenticate with API
            OauthToken oauthToken = canvasAPI.getOauthToken();
            CanvasApiFactory apiFactory = canvasAPI.getApiFactory();

            // Test connection
            AccountReader acctReader = apiFactory.getReader(AccountReader.class, oauthToken);
            Account rootAccount = acctReader.getSingleAccount("1").get();
            log.debug("Connected to Canvas LMS API at '" + rootAccount.getName() + "'.");

            // API readers
            UserReader userReader = apiFactory.getReader(UserReader.class, oauthToken, 100);
            EnrollmentWriter enrollmentWriter = apiFactory.getWriter(EnrollmentWriter.class, oauthToken);
            RoleReader roleReader = apiFactory.getReader(RoleReader.class, oauthToken, 100);

            // Get student role
            ListRolesOptions roleOptions = new ListRolesOptions("1");
            Role studentRole = roleReader.listRoles(roleOptions).stream().filter(role -> role.getBaseRoleType().equalsIgnoreCase("student")
                    || role.getLabel().equalsIgnoreCase("student")).findFirst().orElse(null);


            /*
             * Look through local enrollments
             * And synchronize it to Canvas LMS
             */
            for (RoomLink roomLink : classRoom.getEnrollments()) {

                Student student = roomLink.getStudent();
                String name = student.getFirstName() + " " + student.getLastName();

                // Skip existing enrollments
                if (roomLink.getCanvasStatus() == CanvasStatus.EXISTS) {
                    continue;
                }

                // Find user in Canvas LMS and continue if success
                getUser(userReader, student, true).ifPresentOrElse(user -> {
                    try {

                        // Enroll user in Canvas LMS
                        Enrollment enroll = new Enrollment();
                        enroll.setUserId(String.valueOf(user.getId()));
                        enroll.setRoleId(studentRole.getId());
                        enroll.setSisCourseId(classRoom.getShortName());
                        enroll.setCourseId(roomLink.getCourse().getCanvasId());
                        enroll.setRole("student");
                        enrollmentWriter.enrollUserInCourse(enroll).ifPresentOrElse(result -> {
                            // Successful enrollment
                            roomLink.setCanvasId(result.getId());
                            roomLink.setCanvasStatus(CanvasStatus.EXISTS);
                            enrollmentService.saveChanges(roomLink);
                            log.debug("Enrolled '" + name + "' to '" + classRoom.getShortName() + "'.");
                        }, () -> {
                            // Failed to enroll user to Canvas course
                            roomLink.setCanvasStatus(CanvasStatus.MISSING);
                            enrollmentService.saveChanges(roomLink);
                            log.warn("Failed to enroll '" + name + "'.");
                        });

                    } catch (Exception ex) {
                        // An unknown error occurred while enrolling user
                        roomLink.setCanvasStatus(CanvasStatus.MISSING);
                        enrollmentService.saveChanges(roomLink);
                        log.error("Failed to enroll '" + name + "'.", ex);
                    }

                }, () -> {
                    // No user was found in Canvas LMS via API
                    roomLink.setCanvasStatus(CanvasStatus.MISSING);
                    enrollmentService.saveChanges(roomLink);
                    log.warn("Could not find '" + name + "', and was thereby not enrolled to '" + classRoom.getShortName() + "'.");
                });
            }
            return true;

        } catch (IOException ex) {
            // Connection or authentication error
            log.error("Failed to connect to Canvas LMS API.", ex);
            return false;
        }
    }

    public void test(Classroom classRoom) throws IOException {

        log.info("Testing...");

        OauthToken oauthToken = canvasAPI.getOauthToken();
        CanvasApiFactory apiFactory = canvasAPI.getApiFactory();

        String canvasCourseId = String.valueOf(classRoom.getCanvasId());
//        List<Student> students = classRoom.getStudents();
        List<CourseApplication> courseApplications = courseService.getCourseApplications(classRoom);

        SubmissionReader submissionReader = apiFactory.getReader(SubmissionReader.class, oauthToken);
        AssignmentReader assignmentReader = apiFactory.getReader(AssignmentReader.class, oauthToken);


        ListCourseAssignmentsOptions assignmentsOptions = new ListCourseAssignmentsOptions(canvasCourseId);
        List<Assignment> assignments = assignmentReader.listCourseAssignments(assignmentsOptions);
        List<Submission> submissions = new ArrayList<>();
        for (Assignment assignment : assignments) {
            GetSubmissionsOptions submissionsOptions = new GetSubmissionsOptions(canvasCourseId, assignment.getId());
            submissions.addAll(submissionReader.getCourseSubmissions(submissionsOptions));
        }
        for (Submission submission : submissions) {
            int canvasUserId = submission.getUserId();
            String submissionType = submission.getSubmissionType();
            Date submissionDate = submission.getSubmittedAt();
            if (submissionDate == null || submission.getLate() == null ||
                    submission.getLate() || submissionType == null) {
                log.warn("Student with user id '" + canvasUserId + "' have not delivered assignment for '" + classRoom.getLongName() + "' course.");
                continue;
            }
            courseApplications.stream()
                    .filter(courseApplication -> courseApplication.getStudent().getCanvasId() == canvasUserId)
                    .findAny().ifPresentOrElse((courseApplication) -> {
                Student student = courseApplication.getStudent();
                courseApplication.setStatus(ApplicationStatus.FINISHED);
                courseService.updateCourseApplication(courseApplication);
                log.info("Student '" + student.getFullName() + "' have delivered assignment for '" + classRoom.getLongName() + "' course.");
            }, () -> {
                log.warn("Unable to find student by user id '" + canvasUserId + "' for submission in '" + classRoom.getLongName() + "' course.");
            });
        }

        log.info("Passed test!");


    }

    // Synchronizes users in course with Canvas LMS.
    public boolean syncUsersReadOnly(Classroom classRoom) {

        try {

            test(classRoom);

            List<Student> students = classRoom.getStudents().stream().filter(student -> student.getCanvasStatus() != CanvasStatus.EXISTS)
                    .collect(Collectors.toList());
            if (students.isEmpty()) return true;

            // Authenticate with API
            OauthToken oauthToken = canvasAPI.getOauthToken();
            CanvasApiFactory apiFactory = canvasAPI.getApiFactory();

            // Test connection
            AccountReader acctReader = apiFactory.getReader(AccountReader.class, oauthToken);
            Account rootAccount = acctReader.getSingleAccount("1").get();
            log.debug("Connected to Canvas LMS API at '" + rootAccount.getName() + "'.");

            // User Canvas API reader
            UserReader userReader = apiFactory.getReader(UserReader.class, oauthToken, 100);

            /*
             * Loop through local students
             * And update locally stored information
             */
            for (Student student : students) {

                // Find user in Canvas LMS and continue if success
                getUser(userReader, student, false).ifPresentOrElse(user -> {
                    // Update user status
                    student.setCanvasStatus(CanvasStatus.EXISTS);
                    student.setCanvasId(user.getId());
                    studentService.saveChanges(student);
                }, () -> {
                    // Update user status
                    student.setCanvasStatus(CanvasStatus.MISSING);
                    studentService.saveChanges(student);
                });
            }
            return true;

        } catch (IOException ex) {
            // Connection or authentication error
            log.error("Failed to connect to Canvas LMS API.", ex);
            return false;
        }
    }

    // Synchronizes local course data with Canvas LMS.
    public boolean syncCourseReadOnly(Classroom classRoom) {
        try {

            test(classRoom);

            // Authenticate with API
            OauthToken oauthToken = canvasAPI.getOauthToken();
            CanvasApiFactory apiFactory = canvasAPI.getApiFactory();

            // Test connection
            AccountReader acctReader = apiFactory.getReader(AccountReader.class, oauthToken);
            Account rootAccount = acctReader.getSingleAccount("1").get();
            log.debug("Connected to Canvas LMS API at '" + rootAccount.getName() + "'.");

            // Course Canvas API reader
            CourseReader courseReader = apiFactory.getReader(CourseReader.class, oauthToken, 100);

            // Search settings for course-search
            String courseId = "sis_course_id:" + classRoom.getShortName();
            GetSingleCourseOptions courseOptions = new GetSingleCourseOptions(courseId);

            // Search for course and continue if success
            courseReader.getSingleCourse(courseOptions).ifPresentOrElse(course -> {
                // Update locally stored course data
                classRoom.setCanvasStatus(CanvasStatus.EXISTS);
                classRoom.setCanvasId(course.getId());
                courseService.saveChanges(classRoom);
                log.debug("Found '" + classRoom.getShortName() + "' in Canvas. Local status updated to EXISTS.");
            }, () -> {
                // Fails to find course and mark it as missing
                classRoom.setCanvasStatus(CanvasStatus.MISSING);
                courseService.saveChanges(classRoom);
                log.warn("Could not find '" + classRoom.getShortName() + "' in Canvas. Local status updated to MISSING.");
            });
            return true;

        } catch (IOException ex) {
            // Connection or authentication error
            log.error("Failed to connect to Canvas LMS API.", ex);
            return false;
        }
    }

    // Search for user in Canvas LMS.
    public Optional<User> getUser(UserReader userReader, Student student, boolean allowNameSearch) throws IOException {

        // Search for user in Canvas LMS with name
        String name = student.getFirstName() + " " + student.getLastName();
        GetUsersInAccountOptions options = new GetUsersInAccountOptions("1");

        // Email search result
        options.searchTerm(student.getEmail());
        List<User> mailSearchResult = userReader.getUsersInAccount(options);
        Optional<User> opt = mailSearchResult.stream().filter(user -> (user.getLoginId() != null && user.getLoginId().equalsIgnoreCase(student.getEmail())
                || (user.getEmail() != null && user.getEmail().equalsIgnoreCase(student.getEmail())))).findFirst();

        // If email search success
        if (opt.isPresent()) {
            return opt;
        }
        // Alternatively search by name
        else {
            // Search by name and return result
            options.searchTerm(name);
            List<User> nameSearchResult = userReader.getUsersInAccount(options);
            opt = nameSearchResult.stream().filter(user -> user.getName().equalsIgnoreCase(name)).findFirst();
            return opt;
        }
    }

}