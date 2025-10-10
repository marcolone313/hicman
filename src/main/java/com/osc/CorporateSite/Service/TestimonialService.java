package com.osc.CorporateSite.Service;

import com.osc.CorporateSite.Model.Testimonial;
import com.osc.CorporateSite.Repository.TestimonialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Ottiene tutte le testimonianze pubblicate ordinate per data (più recenti prima)
     */
    public List<Testimonial> getAllPublishedTestimonials() {
        return testimonialRepository.findByPublishedTrueOrderByPublishedDateDesc();
    }

    /**
     * Ottiene tutte le testimonianze (per admin) ordinate per data
     */
    public List<Testimonial> getAllTestimonials() {
        return testimonialRepository.findAll(Sort.by(Sort.Direction.DESC, "publishedDate"));
    }

    /**
     * Ottiene una testimonianza pubblicata per ID
     */
    public Optional<Testimonial> getPublishedTestimonialById(Long id) {
        return testimonialRepository.findByIdAndPublishedTrue(id);
    }

    /**
     * Ottiene una testimonianza per ID (per admin, anche non pubblicata)
     */
    public Optional<Testimonial> getTestimonialById(Long id) {
        return testimonialRepository.findById(id);
    }

    /**
     * Salva una nuova testimonianza
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
     * Rimuove la pubblicazione di una testimonianza
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

    /**
     * Conta il numero di testimonianze pubblicate
     */
    public long countPublishedTestimonials() {
        return testimonialRepository.countByPublishedTrue();
    }

    /**
     * Cerca testimonianze per nome fonte (per admin)
     */
    public List<Testimonial> searchBySourceName(String sourceName) {
        return testimonialRepository.findBySourceNameContainingIgnoreCase(sourceName);
    }

    /**
     * Conta tutte le testimonianze (per dashboard admin)
     */
    public long countAllTestimonials() {
        return testimonialRepository.count();
    }

    /**
     * Ottiene le ultime N testimonianze pubblicate (per dashboard admin)
     */
    public List<Testimonial> getLatestTestimonials(int limit) {
        return testimonialRepository.findByPublishedTrueOrderByPublishedDateDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Ottiene testimonianze pubblicate (alias per compatibilità)
     */
    public List<Testimonial> getPublishedTestimonials() {
        return getAllPublishedTestimonials();
    }

    /**
     * Ottiene testimonianze in bozza (non pubblicate)
     */
    public List<Testimonial> getDraftTestimonials() {
        return testimonialRepository.findByPublished(false);
    }

    /**
     * Salva una testimonianza (create o update)
     */
    public Testimonial saveTestimonial(Testimonial testimonial) {
        if (testimonial.getId() == null) {
            // Nuova testimonianza
            return createTestimonial(testimonial);
        } else {
            // Update esistente
            return testimonialRepository.save(testimonial);
        }
    }

    /**
     * Sposta una testimonianza in su (riordino)
     */
    public void moveUp(Long id) {
        // Implementazione semplice: cambia displayOrder
        // Per ora placeholder - implementa logica di riordino se necessario
        Optional<Testimonial> testimonialOpt = testimonialRepository.findById(id);
        if (testimonialOpt.isPresent()) {
            // Logica riordino da implementare se serve campo displayOrder
            testimonialRepository.save(testimonialOpt.get());
        }
    }

    /**
     * Sposta una testimonianza in giù (riordino)
     */
    public void moveDown(Long id) {
        // Implementazione semplice: cambia displayOrder
        // Per ora placeholder - implementa logica di riordino se necessario
        Optional<Testimonial> testimonialOpt = testimonialRepository.findById(id);
        if (testimonialOpt.isPresent()) {
            // Logica riordino da implementare se serve campo displayOrder
            testimonialRepository.save(testimonialOpt.get());
        }
    }

    /**
     * Elimina multiple testimonianze
     */
    public int deleteTestimonials(Long[] ids) {
        int deleted = 0;
        for (Long id : ids) {
            if (testimonialRepository.existsById(id)) {
                testimonialRepository.deleteById(id);
                deleted++;
            }
        }
        return deleted;
    }
}