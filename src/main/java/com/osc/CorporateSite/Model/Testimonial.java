package com.osc.CorporateSite.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entity per le testimonianze (Dicono di Noi)
 * Rappresenta citazioni e riferimenti da media e partner
 */
@Entity
@Table(name = "testimonials")
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Citazione/Testimonianza principale
     */
    @NotBlank(message = "La citazione è obbligatoria")
    @Size(max = 2000, message = "La citazione non può superare i 2000 caratteri")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String quote;

    /**
     * Nome della fonte (testata giornalistica, azienda, persona)
     */
    @NotBlank(message = "Il nome della fonte è obbligatorio")
    @Size(max = 200, message = "Il nome della fonte non può superare i 200 caratteri")
    @Column(nullable = false)
    private String sourceName;

    /**
     * Ruolo o descrizione della fonte (es. "Financial Times", "CEO di XYZ")
     */
    @Size(max = 200, message = "Il ruolo non può superare i 200 caratteri")
    private String sourceRole;

    /**
     * URL del logo della fonte (opzionale)
     */
    @Size(max = 500, message = "L'URL del logo non può superare i 500 caratteri")
    private String logoUrl;

    /**
     * Link esterno all'articolo o fonte originale
     */
    @Size(max = 500, message = "Il link esterno non può superare i 500 caratteri")
    private String externalLink;

    /**
     * Data di pubblicazione della testimonianza
     */
    @Column(nullable = false)
    private LocalDateTime publishedDate;

    /**
     * Stato di pubblicazione (true = visibile pubblicamente)
     */
    @Column(nullable = false)
    private boolean published = false;

    /**
     * Data di creazione del record
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data ultimo aggiornamento
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ========== CONSTRUCTORS ==========

    public Testimonial() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.publishedDate = LocalDateTime.now();
    }

    public Testimonial(String quote, String sourceName) {
        this();
        this.quote = quote;
        this.sourceName = sourceName;
    }

    // ========== LIFECYCLE CALLBACKS ==========

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Verifica se la testimonianza ha un logo
     */
    public boolean hasLogo() {
        return logoUrl != null && !logoUrl.trim().isEmpty();
    }

    /**
     * Verifica se la testimonianza ha un link esterno
     */
    public boolean hasExternalLink() {
        return externalLink != null && !externalLink.trim().isEmpty();
    }

    /**
     * Ottiene un estratto della citazione (per card)
     * @param maxLength lunghezza massima
     * @return estratto con "..." se troncato
     */
    public String getExcerpt(int maxLength) {
        if (quote == null) return "";
        
        if (quote.length() <= maxLength) {
            return quote;
        }
        
        return quote.substring(0, maxLength).trim() + "...";
    }

    // ========== GETTERS & SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceRole() {
        return sourceRole;
    }

    public void setSourceRole(String sourceRole) {
        this.sourceRole = sourceRole;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========== TOSTRING ==========

    @Override
    public String toString() {
        return "Testimonial{" +
                "id=" + id +
                ", sourceName='" + sourceName + '\'' +
                ", published=" + published +
                ", publishedDate=" + publishedDate +
                '}';
    }
}