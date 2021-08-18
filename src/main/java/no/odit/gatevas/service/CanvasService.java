package no.odit.gatevas.service;

import edu.ksu.canvas.CanvasApiFactory;
import edu.ksu.canvas.interfaces.*;
import edu.ksu.canvas.model.Account;
import edu.ksu.canvas.model.Enrollment;
import edu.ksu.canvas.model.Role;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.GetSingleCourseOptions;
import edu.ksu.canvas.requestOptions.GetUsersInAccountOptions;
import edu.ksu.canvas.requestOptions.ListRolesOptions;
import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.model.RoomLink;
import no.odit.gatevas.model.Student;
import no.odit.gatevas.type.CanvasStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CanvasService {

    @Autowired
    private ApiService apiService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Add students to course in Canvas LMS.
     *
     * @param classRoom Course that students shall join
     * @return Whenever the enrollment fails or not
     */
    public boolean enrollStudents(Classroom classRoom) {
        try {

            // Authenticate with API
            OauthToken oauthToken = apiService.getOauthToken();
            CanvasApiFactory apiFactory = apiService.getApiFactory();

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
                getUser(userReader, student).ifPresentOrElse(user -> {

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

    /**
     * Synchronizes users in course with Canvas LMS.
     *
     * @param classRoom Course that shall be synchronized
     * @return Whenever the synchronization fails or not
     */
    public boolean syncUsersReadOnly(Classroom classRoom) {

        try {

            List<Student> students = classRoom.getStudents().stream().filter(student -> student.getCanvasStatus() != CanvasStatus.EXISTS)
                    .collect(Collectors.toList());
            if (students.isEmpty()) return true;

            // Authenticate with API
            OauthToken oauthToken = apiService.getOauthToken();
            CanvasApiFactory apiFactory = apiService.getApiFactory();

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
                getUser(userReader, student).ifPresentOrElse(user -> {

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

    /**
     * Synchronizes local course data with Canvas LMS.
     *
     * @param classRoom Course that shall be synchronized
     * @return Whenever the synchronization fails or not
     */
    public boolean syncCourseReadOnly(Classroom classRoom) {
        try {

            // Authenticate with API
            OauthToken oauthToken = apiService.getOauthToken();
            CanvasApiFactory apiFactory = apiService.getApiFactory();

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

    /**
     * Search for user in Canvas LMS.
     *
     * @param userReader Authenticated UserReader object
     * @param student    Student that shall be search after
     * @return Empty or populated with User object
     * @throws IOException Fails to connect to Canvas LMS API
     */
    public Optional<User> getUser(UserReader userReader, Student student) throws IOException {

        // Search for user in Canvas LMS with name
        String name = student.getFirstName() + " " + student.getLastName();
        GetUsersInAccountOptions options = new GetUsersInAccountOptions("1");
        options.searchTerm(name);
        List<User> nameSearchResult = userReader.getUsersInAccount(options);

        // Name search result
        Optional<User> opt = nameSearchResult.stream().filter(user -> user.getName().equalsIgnoreCase(name)).findFirst();

        // If name search success
        if (opt.isPresent()) {
            return opt;
        }
        // Alternatively search by email
        else {
            // Search by email and return result
            options.searchTerm(student.getEmail());
            List<User> mailSearchResult = userReader.getUsersInAccount(options);
            opt = mailSearchResult.stream().filter(user -> (user.getLoginId() != null && user.getLoginId().equalsIgnoreCase(student.getEmail())
                    || (user.getEmail() != null && user.getEmail().equalsIgnoreCase(student.getEmail())))).findFirst();
            return opt;
        }
    }

}