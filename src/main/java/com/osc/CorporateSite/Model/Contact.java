package com.osc.CorporateSite.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class Contact {
    
    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;
    
    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;
    
    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Inserisci un indirizzo email valido")
    private String email;
    
    private String telefono;
    
    private String servizio;
    
    @NotBlank(message = "Il messaggio è obbligatorio")
    @Size(min = 10, message = "Il messaggio deve contenere almeno 10 caratteri")
    private String messaggio;
    
    //C h
    private String website;
}