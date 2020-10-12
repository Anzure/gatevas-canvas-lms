package no.odit.gatevas.service;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.ksu.canvas.CanvasApiFactory;
import edu.ksu.canvas.interfaces.AccountReader;
import edu.ksu.canvas.interfaces.CourseReader;
import edu.ksu.canvas.interfaces.CourseWriter;
import edu.ksu.canvas.interfaces.EnrollmentReader;
import edu.ksu.canvas.interfaces.EnrollmentWriter;
import edu.ksu.canvas.interfaces.UserReader;
import edu.ksu.canvas.interfaces.UserWriter;
import edu.ksu.canvas.model.Account;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.GetSingleCourseOptions;
import edu.ksu.canvas.requestOptions.GetUsersInCourseOptions;
import no.odit.gatevas.model.Classroom;
import no.odit.gatevas.type.CanvasStatus;

@Service
public class CanvasService {

	private static final Logger log = LoggerFactory.getLogger(CanvasService.class);

	@Autowired
	private ApiService apiService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private StudentService studentService;

	@Autowired
	private EnrollmentService enrollmentService;

	public boolean syncCourseReadOnly(Classroom classRoom) {
		try {

			OauthToken oauthToken = apiService.getOauthToken();
			CanvasApiFactory apiFactory = apiService.getApiFactory();

			AccountReader acctReader = apiFactory.getReader(AccountReader.class, oauthToken);
			Account rootAccount = acctReader.getSingleAccount("1").get();
			log.debug("Connected to Canvas LMS API at '" + rootAccount.getName() + "'.");

			UserReader userReader = apiFactory.getReader(UserReader.class, oauthToken, 100);
			UserWriter userWriter = apiFactory.getWriter(UserWriter.class, oauthToken);

			CourseReader courseReader = apiFactory.getReader(CourseReader.class, oauthToken, 100);
			CourseWriter courseWriter = apiFactory.getWriter(CourseWriter.class, oauthToken);

			EnrollmentReader enrollmentReader = apiFactory.getReader(EnrollmentReader.class, oauthToken, 100);
			EnrollmentWriter enrollmentWriter = apiFactory.getWriter(EnrollmentWriter.class, oauthToken);


			String courseId = "sis_course_id:" + classRoom.getShortName();
			GetSingleCourseOptions courseOptions = new GetSingleCourseOptions(courseId);

			GetUsersInCourseOptions usersOptions = new GetUsersInCourseOptions(courseId);
			List<User> users = userReader.getUsersInCourse(usersOptions);

			courseReader.getSingleCourse(courseOptions).ifPresentOrElse(course -> {

				classRoom.setCanvasStatus(CanvasStatus.EXISTS);
				courseService.saveChanges(classRoom);
				log.debug("Found '" + classRoom.getShortName() + "' in Canvas. Local status updated to EXISTS.");

				users.forEach(user -> {
					studentService.getUserByEmail(user.getEmail()).ifPresent(student -> {
						student.setCanvasStatus(CanvasStatus.EXISTS);
						enrollmentService.getEnrollment(student, classRoom).ifPresent(enrollment -> {
							enrollment.setCanvasStatus(CanvasStatus.EXISTS);
							enrollmentService.saveChanges(enrollment);
						});
						studentService.saveChanges(student);
					});
				});
				log.debug("Found " + users.size() + " users in Canvas course. Local status updated to EXISTS.");

			}, () -> {

				classRoom.setCanvasStatus(CanvasStatus.EXISTS);
				courseService.saveChanges(classRoom);
				log.warn("Could not find '" + classRoom.getShortName() + "' in Canvas. Local status updated to MISSING.");

			});
			return true;


		} catch (IOException ex) {
			log.error("Failed to connect to Canvas LMS API.", ex);
			return false;
		}
	}


}