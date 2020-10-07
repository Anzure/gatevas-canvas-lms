//package no.odit.gatevas.controller;
//
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import no.odit.gatevas.service.TokenService;
//
//@RestController
//public class CodeController {
//	
//	@Autowired
//	private TokenService tokenService;
//
//	@GetMapping("/oauth_complete")
//	public String giveCode(String code) {
//		
//		tokenService.authCode = Optional.of(code);
//		
//		return "Thanks";
//	}
//	
//}