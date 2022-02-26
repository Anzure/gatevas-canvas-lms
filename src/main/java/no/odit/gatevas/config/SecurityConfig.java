package no.odit.gatevas.config;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class SecurityConfig {

    @Value("${gatevas.security.crypto_key}")
    private String encryptionKey;

    @Value("${gatevas.security.crypto_test}")
    private String encryptionTest;

    @PostConstruct
    public void init(){
        String testData = "f62FWnF4aT6bsE6IF9hY";
        StrongTextEncryptor textEncryptor = getTextEncryptor();
        String encryptTest = textEncryptor.encrypt(testData);
        if (!encryptionTest.isEmpty() && testData.equalsIgnoreCase(textEncryptor.decrypt(encryptionTest))){
            log.info("Encryption test success: " + encryptTest);
        }
        else {
            log.error("Encryption test failed: " + encryptTest);
            System.exit(0);
        }
    }

    @Bean
    public StrongTextEncryptor getTextEncryptor() {
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        return textEncryptor;
    }

}