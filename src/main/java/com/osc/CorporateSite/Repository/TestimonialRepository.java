package com.osc.CorporateSite.Repository;

import com.osc.CorporateSite.Model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per le testimonianze (Dicono di Noi)
 */
@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    /**
     * Trova tutte le testimonianze pubblicate ordinate per data (più recenti prime)
     */
    List<Testimonial> findByPublishedTrueOrderByPublishedDateDesc();

    /**
     * Trova una testimonianza pubblicata per ID
     */
    Optional<Testimonial> findByIdAndPublishedTrue(Long id);

    /**
     * Conta le testimonianze pubblicate
     */
    long countByPublishedTrue();

    /**
     * Cerca testimonianze per nome fonte (case insensitive)
     */
    List<Testimonial> findBySourceNameContainingIgnoreCase(String sourceName);

    /**
     * Trova testimonianze per stato pubblicazione
     */
    List<Testimonial> findByPublished(boolean published);
}