package com.hicman.CorporateSite.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hicman.CorporateSite.Model.Testimonial;
import com.hicman.CorporateSite.Repository.TestimonialRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione delle testimonianze (Dicono di Noi)
 */
@Service
@Transactional
public class TestimonialService {

    @Autowired
    private TestimonialRepository testimonialRepository;

    // ==================== METODI PUBBLICI ====================

    /**
     * Ottiene tutte le testimonianze pubblicate ordinate per data (più recenti prima)
     */
    public List<Testimonial> getAllPublishedTestimonials() {
        return testimonialRepository.findByPublishedTrueOrderByPublishedDateDesc();
    }

    /**
     * Ottiene una testimonianza pubblicata per ID
     */
    public Optional<Testimonial> getPublishedTestimonialById(Long id) {
        return testimonialRepository.findByIdAndPublishedTrue(id);
    }

    // ==================== METODI ADMIN ====================

    /**
     * Ottiene tutte le testimonianze (per admin) ordinate per data
     */
    public List<Testimonial> getAllTestimonials() {
        return testimonialRepository.findAll(Sort.by(Sort.Direction.DESC, "publishedDate"));
    }

    /**
     * Ottiene tutte le testimonianze pubblicate
     */
    public List<Testimonial> getPublishedTestimonials() {
        return testimonialRepository.findByPublishedTrueOrderByPublishedDateDesc();
    }

    /**
     * Ottiene tutte le testimonianze in bozza
     */
    public List<Testimonial> getDraftTestimonials() {
        return testimonialRepository.findByPublishedFalseOrderByPublishedDateDesc();
    }

    /**
     * Ottiene una testimonianza per ID (per admin, anche non pubblicata)
     */
    public Optional<Testimonial> getTestimonialById(Long id) {
        return testimonialRepository.findById(id);
    }

    /**
     * Ottiene le ultime N testimonianze (tutte, anche bozze) per dashboard admin
     */
    public List<Testimonial> getLatestTestimonials(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedDate"));
        return testimonialRepository.findAll(pageable).getContent();
    }

    /**
     * Salva una nuova testimonianza o aggiorna esistente
     */
    public Testimonial saveTestimonial(Testimonial testimonial) {
        // Se è una nuova testimonianza, imposta la data di pubblicazione
        if (testimonial.getId() == null && testimonial.getPublishedDate() == null) {
            testimonial.setPublishedDate(LocalDateTime.now());
        }
        
        // Se non ha displayOrder, impostalo all'ultimo
        if (testimonial.getDisplayOrder() == 0) {
            long maxOrder = testimonialRepository.count();
            testimonial.setDisplayOrder((int) maxOrder + 1);
        }
        
        return testimonialRepository.save(testimonial);
    }

    /**
     * Crea una nuova testimonianza
     */
    public Testimonial createTestimonial(Testimonial testimonial) {
        // Imposta la data di creazione se non presente
        if (testimonial.getPublishedDate() == null) {
            testimonial.setPublishedDate(LocalDateTime.now());
        }
        return testimonialRepository.save(testimonial);
    }

    /**
     * Aggiorna una testimonianza esistente
     */
    public Testimonial updateTestimonial(Long id, Testimonial updatedTestimonial) {
        Optional<Testimonial> existing = testimonialRepository.findById(id);
        
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Testimonianza non trovata con ID: " + id);
        }
        
        Testimonial testimonial = existing.get();
        
        // Aggiorna i campi
        testimonial.setQuote(updatedTestimonial.getQuote());
        testimonial.setSourceName(updatedTestimonial.getSourceName());
        testimonial.setSourceRole(updatedTestimonial.getSourceRole());
        testimonial.setLogoUrl(updatedTestimonial.getLogoUrl());
        testimonial.setExternalLink(updatedTestimonial.getExternalLink());
        testimonial.setPublishedDate(updatedTestimonial.getPublishedDate());
        testimonial.setPublished(updatedTestimonial.isPublished());
        
        return testimonialRepository.save(testimonial);
    }

    /**
     * Elimina una testimonianza
     */
    public void deleteTestimonial(Long id) {
        if (!testimonialRepository.existsById(id)) {
            throw new IllegalArgumentException("Testimonianza non trovata con ID: " + id);
        }
        testimonialRepository.deleteById(id);
    }

    /**
     * Elimina multiple testimonianze
     */
    @Transactional
    public int deleteTestimonials(Long[] testimonialIds) {
        int count = 0;
        for (Long id : testimonialIds) {
            if (testimonialRepository.existsById(id)) {
                testimonialRepository.deleteById(id);
                count++;
            }
        }
        return count;
    }

    /**
     * Pubblica una testimonianza
     */
    public Testimonial publishTestimonial(Long id) {
        Optional<Testimonial> testimonialOpt = testimonialRepository.findById(id);
        
        if (testimonialOpt.isEmpty()) {
            throw new IllegalArgumentException("Testimonianza non trovata con ID: " + id);
        }
        
        Testimonial testimonial = testimonialOpt.get();
        testimonial.setPublished(true);
        
        // Imposta la data di pubblicazione se non presente
        if (testimonial.getPublishedDate() == null) {
            testimonial.setPublishedDate(LocalDateTime.now());
        }
        
        return testimonialRepository.save(testimonial);
    }

    /**
     * Depubblica una testimonianza
     */
    public Testimonial unpublishTestimonial(Long id) {
        Optional<Testimonial> testimonialOpt = testimonialRepository.findById(id);
        
        if (testimonialOpt.isEmpty()) {
            throw new IllegalArgumentException("Testimonianza non trovata con ID: " + id);
        }
        
        Testimonial testimonial = testimonialOpt.get();
        testimonial.setPublished(false);
        
        return testimonialRepository.save(testimonial);
    }

    // ==================== RIORDINO ====================

    /**
     * Sposta una testimonianza verso l'alto (decrementa displayOrder)
     */
    @Transactional
    public void moveUp(Long id) {
        Optional<Testimonial> testimonialOpt = testimonialRepository.findById(id);
        if (testimonialOpt.isEmpty()) {
            throw new IllegalArgumentException("Testimonianza non trovata");
        }
        
        Testimonial testimonial = testimonialOpt.get();
        int currentOrder = testimonial.getDisplayOrder();
        
        // Trova la testimonianza con displayOrder immediatamente superiore
        List<Testimonial> above = testimonialRepository.findByDisplayOrderLessThanOrderByDisplayOrderDesc(currentOrder);
        
        if (!above.isEmpty()) {
            Testimonial swapWith = above.get(0);
            int swapOrder = swapWith.getDisplayOrder();
            
            // Scambia gli ordini
            swapWith.setDisplayOrder(currentOrder);
            testimonial.setDisplayOrder(swapOrder);
            
            testimonialRepository.save(swapWith);
            testimonialRepository.save(testimonial);
        }
    }

    /**
     * Sposta una testimonianza verso il basso (incrementa displayOrder)
     */
    @Transactional
    public void moveDown(Long id) {
        Optional<Testimonial> testimonialOpt = testimonialRepository.findById(id);
        if (testimonialOpt.isEmpty()) {
            throw new IllegalArgumentException("Testimonianza non trovata");
        }
        
        Testimonial testimonial = testimonialOpt.get();
        int currentOrder = testimonial.getDisplayOrder();
        
        // Trova la testimonianza con displayOrder immediatamente inferiore
        List<Testimonial> below = testimonialRepository.findByDisplayOrderGreaterThanOrderByDisplayOrderAsc(currentOrder);
        
        if (!below.isEmpty()) {
            Testimonial swapWith = below.get(0);
            int swapOrder = swapWith.getDisplayOrder();
            
            // Scambia gli ordini
            swapWith.setDisplayOrder(currentOrder);
            testimonial.setDisplayOrder(swapOrder);
            
            testimonialRepository.save(swapWith);
            testimonialRepository.save(testimonial);
        }
    }

    // ==================== STATISTICHE ====================

    /**
     * Conta tutte le testimonianze (pubblicate e bozze)
     */
    public long countAllTestimonials() {
        return testimonialRepository.count();
    }

    /**
     * Conta solo le testimonianze pubblicate
     */
    public long countPublishedTestimonials() {
        return testimonialRepository.countByPublishedTrue();
    }

    /**
     * Conta solo le bozze
     */
    public long countDraftTestimonials() {
        return testimonialRepository.countByPublishedFalse();
    }
}