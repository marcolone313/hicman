package com.osc.CorporateSite.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "testimonials")
public class Testimonial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il nome dell'autore è obbligatorio")
    @Size(min = 2, max = 100, message = "Il nome dell'autore deve essere tra 2 e 100 caratteri")
    @Column(nullable = false, length = 100)
    private String author;
    
    @Size(max = 100, message = "Il ruolo non può superare i 100 caratteri")
    @Column(name = "author_role", length = 100)
    private String authorRole;
    
    @Size(max = 100, message = "Il nome dell'azienda non può superare i 100 caratteri")
    @Column(name = "company_name", length = 100)
    private String companyName;
    
    @NotBlank(message = "Il contenuto è obbligatorio")
    @Size(min = 10, max = 1000, message = "Il contenuto deve essere tra 10 e 1000 caratteri")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "source_url", length = 500)
    private String sourceUrl;
    
    @Column(name = "is_published")
    private boolean published = false;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Metodo helper per ottenere un estratto breve
    public String getShortContent(int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
    
    // Metodo helper per verificare se ha un URL sorgente
    public boolean hasSourceUrl() {
        return sourceUrl != null && !sourceUrl.trim().isEmpty();
    }
    
    // Metodo helper per ottenere il nome completo
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(author);
        if (authorRole != null && !authorRole.trim().isEmpty()) {
            fullName.append(", ").append(authorRole);
        }
        if (companyName != null && !companyName.trim().isEmpty()) {
            fullName.append(" at ").append(companyName);
        }
        return fullName.toString();
    }
}