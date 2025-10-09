package com.osc.CorporateSite.Service;

import com.osc.CorporateSite.Model.Contact;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    @Value("${lambda.email.api.url}")
    private String lambdaApiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public ContactService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public void processContact(Contact contact) throws Exception {
        logger.info("Iniziando elaborazione contatto per: {} {}", contact.getNome(), contact.getCognome());
        
        try {
            // Prepara i dati per Lambda
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("nome", contact.getNome());
            requestBody.put("cognome", contact.getCognome());
            requestBody.put("email", contact.getEmail());
            requestBody.put("telefono", contact.getTelefono());
            requestBody.put("messaggio", contact.getMessaggio());
            
            // Prepara headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Crea la richiesta
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            logger.info("Tentativo di invio email via Lambda API: {}", lambdaApiUrl);
            
            // Chiama Lambda
            ResponseEntity<Map> response = restTemplate.exchange(
                lambdaApiUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Email inviata con successo via Lambda per: {} {}", 
                    contact.getNome(), contact.getCognome());
            } else {
                logger.error("Errore Lambda - Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                throw new Exception("Lambda API error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Errore durante l'invio dell'email via Lambda per {} {}: {}", 
                contact.getNome(), contact.getCognome(), e.getMessage(), e);
            throw e;
        }
    }
}