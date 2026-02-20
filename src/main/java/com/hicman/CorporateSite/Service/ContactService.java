package com.hicman.CorporateSite.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hicman.CorporateSite.Model.Contact;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    @Value("${mailgun.api.key}")
    private String mailgunApiKey;

    @Value("${mailgun.domain}")
    private String mailgunDomain;

    @Value("${mailgun.from}")
    private String mailgunFrom;

    @Value("${mailgun.to}")
    private String mailgunTo;

    private final RestTemplate restTemplate;

    public ContactService() {
        this.restTemplate = new RestTemplate();
    }

    public void processContact(Contact contact) throws Exception {
        logger.info("Iniziando elaborazione contatto per: {} {}", contact.getNome(), contact.getCognome());

        try {
            String url = "https://api.eu.mailgun.net/v3/" + mailgunDomain + "/messages";

            // Basic Auth header (impostato manualmente per compatibilità con form-urlencoded)
            HttpHeaders headers = new HttpHeaders();
            String auth = Base64.getEncoder().encodeToString(
                ("api:" + mailgunApiKey).getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + auth);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("from", mailgunFrom);
            formData.add("to", mailgunTo);
            formData.add("subject", "Nuovo contatto da " + contact.getNome() + " " + contact.getCognome());
            formData.add("text", buildEmailText(contact));

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

            logger.info("Tentativo di invio email via Mailgun API");

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Email inviata con successo via Mailgun per: {} {}",
                    contact.getNome(), contact.getCognome());
            } else {
                logger.error("Errore Mailgun - Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
                throw new Exception("Mailgun API error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Errore durante l'invio dell'email via Mailgun per {} {}: {}",
                contact.getNome(), contact.getCognome(), e.getMessage(), e);
            throw e;
        }
    }

    private String buildEmailText(Contact contact) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nuovo messaggio dal form contatti Hicman\n");
        sb.append("==========================================\n\n");
        sb.append("Nome: ").append(contact.getNome()).append("\n");
        sb.append("Cognome: ").append(contact.getCognome()).append("\n");
        sb.append("Email: ").append(contact.getEmail()).append("\n");
        if (contact.getTelefono() != null && !contact.getTelefono().isEmpty()) {
            sb.append("Telefono: ").append(contact.getTelefono()).append("\n");
        }
        if (contact.getServizio() != null && !contact.getServizio().isEmpty()) {
            sb.append("Servizio: ").append(contact.getServizio()).append("\n");
        }
        sb.append("\nMessaggio:\n").append(contact.getMessaggio()).append("\n");
        return sb.toString();
    }
}
