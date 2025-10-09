package com.osc.CorporateSite.Service;

import com.osc.CorporateSite.Model.Testimonial;
import com.osc.CorporateSite.Repository.TestimonialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TestimonialService {

    @Autowired
    private TestimonialRepository testimonialRepository;

    // ==================== METODI PUBBLICI ====================

    /**
     * Ottiene tutti i testimonial pubblicati ordinati per displayOrder
     */
    public List<Testimonial> getPublishedTestimonials() {
        return testimonialRepository.findByPublishedTrueOrderByDisplayOrderAsc();
    }

    /**
     * Ottiene i primi N testimonial pubblicati
     */
    public List<Testimonial> getTopPublishedTestimonials(int limit) {
        return testimonialRepository.findByPublishedTrueOrderByDisplayOrderAsc()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Conta i testimonial pubblicati
     */
    public long countPublishedTestimonials() {
        return testimonialRepository.countByPublishedTrue();
    }

    // ==================== METODI ADMIN ====================

    /**
     * Ottiene tutti i testimonial (pubblicati e bozze)
     */
    public List<Testimonial> getAllTestimonials() {
        return testimonialRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * Ottiene tutti i testimonial in bozza
     */
    public List<Testimonial> getDraftTestimonials() {
        return testimonialRepository.findByPublishedFalseOrderByCreatedAtDesc();
    }

    /**
     * Ottiene un testimonial per ID
     */
    public Optional<Testimonial> getTestimonialById(Long id) {
        return testimonialRepository.findById(id);
    }

    /**
     * Ottiene gli ultimi N testimonial creati
     */
    public List<Testimonial> getLatestTestimonials(int limit) {
        return testimonialRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Salva un nuovo testimonial o aggiorna uno esistente
     */
    public Testimonial saveTestimonial(Testimonial testimonial) {
        // Se è un nuovo testimonial
        if (testimonial.getId() == null) {
            testimonial.setCreatedAt(LocalDateTime.now());
            
            // Imposta l'ordine di visualizzazione come ultimo
            if (testimonial.getDisplayOrder() == null || testimonial.getDisplayOrder() == 0) {
                Integer maxOrder = testimonialRepository.findMaxDisplayOrder();
                testimonial.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 1);
            }
        }
        
        return testimonialRepository.save(testimonial);
    }

    /**
     * Elimina un testimonial
     */
    public void deleteTestimonial(Long id) {
        testimonialRepository.deleteById(id);
    }

    /**
     * Elimina più testimonial contemporaneamente
     */
    public int deleteTestimonials(Long[] testimonialIds) {
        int count = 0;
        for (Long id : testimonialIds) {
            try {
                testimonialRepository.deleteById(id);
                count++;
            } catch (Exception e) {
                System.err.println("Errore eliminazione testimonial ID " + id + ": " + e.getMessage());
            }
        }
        return count;
    }

    /**
     * Conta tutti i testimonial
     */
    public long countAllTestimonials() {
        return testimonialRepository.count();
    }

    /**
     * Cambia lo stato di pubblicazione
     */
    public Testimonial togglePublishStatus(Long id) {
        Optional<Testimonial> testimonialOpt = testimonialRepository.findById(id);
        
        if (testimonialOpt.isEmpty()) {
            throw new IllegalArgumentException("Testimonial non trovato con ID: " + id);
        }
        
        Testimonial testimonial = testimonialOpt.get();
        testimonial.setPublished(!testimonial.isPublished());
        
        return testimonialRepository.save(testimonial);
    }

    // ==================== GESTIONE ORDINAMENTO ====================

    /**
     * Sposta un testimonial verso l'alto (diminuisce displayOrder)
     */
    public void moveUp(Long id) {
        Optional<Testimonial> currentOpt = testimonialRepository.findById(id);
        
        if (currentOpt.isEmpty()) {
            throw new IllegalArgumentException("Testimonial non trovato con ID: " + id);
        }
        
        Testimonial current = currentOpt.get();
        Integer currentOrder = current.getDisplayOrder();
        
        // Trova il testimonial con displayOrder immediatamente inferiore
        Optional<Testimonial> previousOpt = testimonialRepository
                .findFirstByDisplayOrderLessThanOrderByDisplayOrderDesc(currentOrder);
        
        if (previousOpt.isPresent()) {
            Testimonial previous = previousOpt.get();
            Integer previousOrder = previous.getDisplayOrder();
            
            // Scambia gli ordini
            current.setDisplayOrder(previousOrder);
            previous.setDisplayOrder(currentOrder);
            
            testimonialRepository.save(current);
            testimonialRepository.save(previous);
        }
    }

    /**
     * Sposta un testimonial verso il basso (aumenta displayOrder)
     */
    public void moveDown(Long id) {
        Optional<Testimonial> currentOpt = testimonialRepository.findById(id);
        
        if (currentOpt.isEmpty()) {
            throw new IllegalArgumentException("Testimonial non trovato con ID: " + id);
        }
        
        Testimonial current = currentOpt.get();
        Integer currentOrder = current.getDisplayOrder();
        
        // Trova il testimonial con displayOrder immediatamente superiore
        Optional<Testimonial> nextOpt = testimonialRepository
                .findFirstByDisplayOrderGreaterThanOrderByDisplayOrderAsc(currentOrder);
        
        if (nextOpt.isPresent()) {
            Testimonial next = nextOpt.get();
            Integer nextOrder = next.getDisplayOrder();
            
            // Scambia gli ordini
            current.setDisplayOrder(nextOrder);
            next.setDisplayOrder(currentOrder);
            
            testimonialRepository.save(current);
            testimonialRepository.save(next);
        }
    }

    /**
     * Sposta un testimonial in una posizione specifica
     */
    public void moveToPosition(Long id, int newPosition) {
        Optional<Testimonial> testimonialOpt = testimonialRepository.findById(id);
        
        if (testimonialOpt.isEmpty()) {
            throw new IllegalArgumentException("Testimonial non trovato con ID: " + id);
        }
        
        Testimonial testimonial = testimonialOpt.get();
        int oldPosition = testimonial.getDisplayOrder();
        
        if (oldPosition == newPosition) {
            return; // Nessun cambiamento necessario
        }
        
        List<Testimonial> allTestimonials = testimonialRepository.findAllByOrderByDisplayOrderAsc();
        
        // Rimuovi il testimonial corrente dalla lista
        allTestimonials.removeIf(t -> t.getId().equals(id));
        
        // Inserisci nella nuova posizione
        allTestimonials.add(newPosition - 1, testimonial);
        
        // Riassegna tutti gli ordini
        for (int i = 0; i < allTestimonials.size(); i++) {
            allTestimonials.get(i).setDisplayOrder(i + 1);
            testimonialRepository.save(allTestimonials.get(i));
        }
    }

    /**
     * Riordina automaticamente tutti i testimonial
     * (utile se ci sono buchi nella sequenza)
     */
    public void reorderAll() {
        List<Testimonial> allTestimonials = testimonialRepository.findAllByOrderByDisplayOrderAsc();
        
        for (int i = 0; i < allTestimonials.size(); i++) {
            allTestimonials.get(i).setDisplayOrder(i + 1);
            testimonialRepository.save(allTestimonials.get(i));
        }
    }

    // ==================== METODI DI UTILITY ====================

    /**
     * Verifica se un testimonial esiste
     */
    public boolean existsById(Long id) {
        return testimonialRepository.existsById(id);
    }

    /**
     * Ottiene testimonial per azienda
     */
    public List<Testimonial> getTestimonialsByCompany(String companyName) {
        return testimonialRepository.findByCompanyNameContainingIgnoreCaseOrderByDisplayOrderAsc(companyName);
    }

    /**
     * Ottiene testimonial per autore
     */
    public List<Testimonial> getTestimonialsByAuthor(String authorName) {
        return testimonialRepository.findByAuthorContainingIgnoreCaseOrderByDisplayOrderAsc(authorName);
    }

    /**
     * Ricerca testimonial per keyword
     */
    public List<Testimonial> searchTestimonials(String keyword) {
        return testimonialRepository.searchTestimonials(keyword);
    }
}