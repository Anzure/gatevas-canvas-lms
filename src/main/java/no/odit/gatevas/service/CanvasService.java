package no.odit.gatevas.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
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
import edu.ksu.canvas.model.Course;
import edu.ksu.canvas.model.Enrollment;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.oauth.OauthTokenRefresher;
import edu.ksu.canvas.oauth.RefreshableOauthToken;
import edu.ksu.canvas.requestOptions.GetUsersInAccountOptions;



@Service
public class CanvasService {

	@Value("${canvas_lms.client_id}")
	private String clientId;

	@Value("${canvas_lms.client_secret}")
	private String clientSecret;

	@Value("${canvas_lms.redirect_uri}")
	private String redirectUri;

	@Value("${canvas_lms.refresh_token}")
	private String refreshToken;

	@PostConstruct
	public void test() {
		try {

			String canvasBaseUrl = "https://fagskolentelemark.instructure.com";
			OauthTokenRefresher tokenRefresher = new OauthTokenRefresher(clientId, clientSecret, canvasBaseUrl);
			OauthToken oauthToken = new RefreshableOauthToken(tokenRefresher, refreshToken);
			CanvasApiFactory apiFactory = new CanvasApiFactory(canvasBaseUrl);
			AccountReader acctReader = apiFactory.getReader(AccountReader.class, oauthToken);
			Account rootAccount = acctReader.getSingleAccount("1").get();
			System.out.println(rootAccount.getName());

			UserReader userReader = apiFactory.getReader(UserReader.class, oauthToken, 100);
			UserWriter userWriter = apiFactory.getWriter(UserWriter.class, oauthToken);
			
			CourseReader courseReader = apiFactory.getReader(CourseReader.class, oauthToken, 100);
			CourseWriter courseWriter = apiFactory.getWriter(CourseWriter.class, oauthToken);
			
			EnrollmentReader enrollmentReader = apiFactory.getReader(EnrollmentReader.class, oauthToken, 100);
			EnrollmentWriter enrollmentWriter = apiFactory.getWriter(EnrollmentWriter.class, oauthToken);
			
			User user = new User();
			user.setEmail("ola.nordmann@outlook.com");
			user.setName("Ola Nordmann");
			user.setLoginId("ola.nordmann@outlook.com");
			
			userWriter.createUser(user);
			
			Course course = new Course();

			
			Enrollment enroll = new Enrollment();
			enroll.setUser(user);
			enrollmentWriter.enrollUserInCourse(enroll);
			
			
			
			
//			GetUsersInAccountOptions options = new GetUsersInAccountOptions("1");
//			options.searchTerm("Andre Mathisen");
//			List<User> users = userReader.getUsersInAccount(options);
//			System.out.println("Found " + users.size() + " users.");
//
//			users.stream()
//			.filter(user -> user.getName().equalsIgnoreCase("Andre Mathisen"))
//			.findFirst().ifPresent(user -> {
//				System.out.println("Found user: " + user.getName());
//			});
//			
//			System.out.println(users.get(0).toJsonObject(false).toString());
//			System.out.println(users.get(0).getName());

			
			
			
//			Optional<User> optUser = userReader.showUserDetails("sis_user_id:Andre.Mathisen@usn.no");
//			optUser.ifPresentOrElse(user -> {
//				System.out.println("Found user: " + user.getName());
//			}, () -> System.out.println("Failed to find user!"));

			System.exit(0);


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


}