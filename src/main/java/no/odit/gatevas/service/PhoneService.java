package no.odit.gatevas.service;

import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import no.odit.gatevas.dao.PhoneRepo;
import no.odit.gatevas.model.Phone;

@Component
public class PhoneService {

	private static final Logger log = LoggerFactory.getLogger(PhoneService.class);

	@Value("${gatevas.sms.sender}")
	private String sender;

	@Value("${gatevas.sms.token}")
	private String token;

	@Value("${gatevas.sms.country}")
	private int country;

	@Autowired
	private PhoneRepo phoneRepo;

	public Phone createPhone(int phoneNumber) {
		Phone phone = new Phone();
		phone.setPhoneNumber(phoneNumber);
		phone.setCountryCode(country);
		return phoneRepo.saveAndFlush(phone);
	}

	public boolean sendSMS(String msg, int phoneNumber) {
		try {
			URL url = new URL("https://gatewayapi.com/rest/mtsms");
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoOutput(true);

			if (sender.length() > 10) sender = sender.substring(0, 11);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(
					"token=" + token
					+ "&sender=" + URLEncoder.encode(sender, "UTF-8")
					+ "&message=" + URLEncoder.encode(msg, "UTF-8")
					+ "&class=premium&priority=VERY_URGENT&recipients.0.msisdn=" + phoneNumber
					);
			wr.close();

			int responseCode = con.getResponseCode();
			log.debug("SMS response code: " + responseCode);
			return responseCode == 200 ? true : false;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}