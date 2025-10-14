package com.hicman.CorporateSite.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Contact {
    
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 2, max = 50, message = "Il nome deve essere tra 2 e 50 caratteri")
    private String nome;
    
    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(min = 2, max = 50, message = "Il cognome deve essere tra 2 e 50 caratteri")
    private String cognome;
    
    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Inserisci un indirizzo email valido")
    private String email;
    
    @Size(max = 20, message = "Il numero di telefono non può superare i 20 caratteri")
    private String telefono;
    
    @Size(max = 100, message = "Il servizio non può superare i 100 caratteri")
    private String servizio;
    
    @NotBlank(message = "Il messaggio è obbligatorio")
    @Size(min = 10, max = 2000, message = "Il messaggio deve contenere tra 10 e 2000 caratteri")
    private String messaggio;
    
    /**
     * Campo honeypot per protezione anti-spam
     * Questo campo è nascosto nel form e non deve essere compilato dagli utenti reali
     * Se viene compilato, è probabilmente un bot
     */
    private String website;
    
    /**
     * Metodo helper per verificare il honeypot
     * @return true se il honeypot è stato compilato (possibile spam)
     */
    public boolean isHoneypotFilled() {
        return website != null && !website.trim().isEmpty();
    }
    
    /**
     * Getter per honeypot (alias per website per retrocompatibilità)
     * @return il valore del campo honeypot
     */
    public String getHoneypot() {
        return website;
    }
    
    /**
     * Setter per honeypot (alias per website per retrocompatibilità)
     * @param honeypot il valore da impostare
     */
    public void setHoneypot(String honeypot) {
        this.website = honeypot;
    }
    
    /**
     * Ottiene il nome completo
     * @return nome e cognome concatenati
     */
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
}