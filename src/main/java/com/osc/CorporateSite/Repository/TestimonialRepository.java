package com.osc.CorporateSite.Repository;

import com.osc.CorporateSite.Model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    // ==================== METODI PUBBLICI ====================
    
    /**
     * Trova tutte le testimonianze pubblicate ordinate per data
     */
    List<Testimonial> findByPublishedTrueOrderByPublishedDateDesc();
    
    /**
     * Trova una testimonianza pubblicata per ID
     */
    Optional<Testimonial> findByIdAndPublishedTrue(Long id);
    
    /**
     * Conta testimonianze pubblicate
     */
    long countByPublishedTrue();

    // ==================== METODI ADMIN ====================
    
    /**
     * Conta bozze
     */
    long countByPublishedFalse();
    
    /**
     * Trova bozze ordinate per data
     */
    List<Testimonial> findByPublishedFalseOrderByPublishedDateDesc();
    
    /**
     * Trova testimonianze con displayOrder minore, ordinate desc (per moveUp)
     */
    List<Testimonial> findByDisplayOrderLessThanOrderByDisplayOrderDesc(int displayOrder);
    
    /**
     * Trova testimonianze con displayOrder maggiore, ordinate asc (per moveDown)
     */
    List<Testimonial> findByDisplayOrderGreaterThanOrderByDisplayOrderAsc(int displayOrder);
}