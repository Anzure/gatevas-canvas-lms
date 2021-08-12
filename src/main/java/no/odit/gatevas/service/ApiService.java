package no.odit.gatevas.service;

import edu.ksu.canvas.CanvasApiFactory;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.oauth.OauthTokenRefresher;
import edu.ksu.canvas.oauth.RefreshableOauthToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

	@Value("${canvas_lms.client_id}")
	private String clientId;

	@Value("${canvas_lms.client_secret}")
	private String clientSecret;

	@Value("${canvas_lms.redirect_uri}")
	private String redirectUri;

	@Value("${canvas_lms.refresh_token}")
	private String refreshToken;

	@Value("${canvas_lms.base_url}")
	private String canvasBaseUrl;

	/**
	 * Gets active oauth token
	 * @return Refreshed oauth token
	 */
	public OauthToken getOauthToken() {
		OauthTokenRefresher tokenRefresher = new OauthTokenRefresher(clientId, clientSecret, canvasBaseUrl);
		OauthToken oauthToken = new RefreshableOauthToken(tokenRefresher, refreshToken);
		return oauthToken;
	}

	/**
	 * Gets current Canvas API factory
	 * @return Connected Canvas API factory
	 */
	public CanvasApiFactory getApiFactory() {
		CanvasApiFactory apiFactory = new CanvasApiFactory(canvasBaseUrl);
		return apiFactory;
	}
}