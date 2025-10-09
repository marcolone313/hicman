package com.osc.CorporateSite.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "blog_posts")
public class BlogPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il titolo è obbligatorio")
    @Size(min = 3, max = 200, message = "Il titolo deve essere tra 3 e 200 caratteri")
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "Il contenuto è obbligatorio")
    @Size(min = 10, message = "Il contenuto deve essere di almeno 10 caratteri")
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "external_link", length = 500)
    private String externalLink;
    
    @Size(max = 100, message = "Il nome della fonte non può superare i 100 caratteri")
    @Column(name = "source_name", length = 100)
    private String sourceName;
    
    @Column(name = "published_date")
    private LocalDateTime publishedDate;
    
    @Column(name = "is_published")
    private boolean published = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Metodo helper per ottenere un estratto del contenuto
    public String getExcerpt(int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
    
    // Metodo helper per verificare se ha un'immagine
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }
    
    // Metodo helper per verificare se ha un link esterno
    public boolean hasExternalLink() {
        return externalLink != null && !externalLink.trim().isEmpty();
    }
}