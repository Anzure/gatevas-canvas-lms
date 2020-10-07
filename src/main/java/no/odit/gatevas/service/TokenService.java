//package no.odit.gatevas.service;
//
//import java.awt.Desktop;
//import java.net.URI;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import javax.annotation.PostConstruct;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import com.google.gson.JsonObject;
//
//import no.odit.gatevas.dao.CanvasTokenRepo;
//import no.odit.gatevas.model.CanvasToken;
//import reactor.core.publisher.Mono;
//
//@Service
//public class TokenService {
//
//	@Value("${canvas_lms.client_id}")
//	private String clientId;
//
//	@Value("${canvas_lms.client_secret}")
//	private String clientSecret;
//
//	@Value("${canvas_lms.redirect_uri}")
//	private String redirectUri;
//
//	public Optional<String> authCode = Optional.empty();
//
//	@Autowired
//	private WebClient webClient;
//
//	@Autowired
//	private CanvasTokenRepo canvasTokenRepo;
//
////	@PostConstruct
//	public void init() {
//
//		getToken();
//
//
//	}
//
//	public CanvasToken getToken() {
//		return canvasTokenRepo.findTop1ByOrderByCreatedAtDesc().orElseGet(() -> {
//
//			// Generate first token
//			while(authCode.isEmpty());
//			JsonObject requestReponse = generateNewToken(authCode.get());
//
//			String refreshToken = requestReponse.get("refresh_token").getAsString();
//			String accessToken = requestReponse.get("access_token").getAsString();
//			int expire = requestReponse.get("expires_in").getAsInt();
//
//			CanvasToken token = new CanvasToken();
//			token.setRefreshToken(refreshToken);
//			token.setAccessToken(accessToken);
//			token.setAccessExpired(LocalDateTime.now().plusSeconds(expire));
//
//			return canvasTokenRepo.saveAndFlush(token);
//
//
//
//		}).ifExpired((expiredToken) -> {
//
//			// Refresh existing token
//			JsonObject requestReponse = refreshToken(expiredToken);
//
//			String accessToken = requestReponse.get("access_token").getAsString();
//			int expire = requestReponse.get("expires_in").getAsInt();
//
//			CanvasToken token = new CanvasToken();
//			token.setRefreshToken(expiredToken.getRefreshToken());
//			token.setAccessToken(accessToken);
//			token.setAccessExpired(LocalDateTime.now().plusSeconds(expire));
//
//			return canvasTokenRepo.saveAndFlush(token);
//		});
//
//	}
//
//	public JsonObject refreshToken(CanvasToken token){
//
//		JsonObject inner = new JsonObject();
//		inner.addProperty("grant_type", "refresh_token");
//		inner.addProperty("client_id", clientId);
//		inner.addProperty("client_secret", clientSecret);
//		inner.addProperty("redirect_uri", clientSecret);
//		inner.addProperty("refresh_token", token.getRefreshToken());
//
//		System.out.println(inner.toString());
//
//		return webClient.post()
//				.uri(builder -> builder
//						.scheme("https")
//						.host("fagskolentelemark.instructure.com")
//						.path("login/oauth2/token")
//						.build())
//				.contentType(MediaType.APPLICATION_JSON)
//				.body(Mono.just(inner.toString()), String.class)
//				.retrieve()
//				.bodyToMono(JsonObject.class)
//				.block();
//
//	}
//
//	public JsonObject generateNewToken(String authCode) {
//
//		JsonObject inner = new JsonObject();
//		inner.addProperty("grant_type", "authorization_code");
//		inner.addProperty("client_id", clientId);
//		inner.addProperty("client_secret", clientSecret);
//		inner.addProperty("redirect_uri", clientSecret);
//		inner.addProperty("code", authCode);
//
//		System.out.println(inner.toString());
//
//		return webClient.post()
//				.uri(builder -> builder
//						.scheme("https")
//						.host("fagskolentelemark.instructure.com")
//						.path("login/oauth2/token")
//						.build())
//				.contentType(MediaType.APPLICATION_JSON)
//				.body(Mono.just(inner.toString()), String.class)
//				.retrieve()
//				.bodyToMono(JsonObject.class)
//				.block();
//
//	}
//
//}