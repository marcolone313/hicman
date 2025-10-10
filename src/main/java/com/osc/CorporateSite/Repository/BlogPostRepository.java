package com.osc.CorporateSite.Repository;

import com.osc.CorporateSite.Model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    // ==================== METODI PUBBLICI ====================
    
    /**
     * Trova tutti i post pubblicati con paginazione, ordinati per data
     */
    Page<BlogPost> findByPublishedTrueOrderByPublishedDateDesc(Pageable pageable);
    
    /**
     * Trova un post pubblicato per ID
     */
    Optional<BlogPost> findByIdAndPublishedTrue(Long id);
    
    /**
     * Trova i primi 10 post pubblicati
     */
    List<BlogPost> findTop10ByPublishedTrueOrderByPublishedDateDesc();
    
    /**
     * Ricerca nei post pubblicati
     */
    @Query("SELECT p FROM BlogPost p WHERE p.published = true AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sourceName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<BlogPost> searchPublishedPosts(@Param("query") String query, Pageable pageable);
    
    /**
     * Conta i post pubblicati
     */
    long countByPublishedTrue();

    // ==================== METODI ADMIN ====================
    
    /**
     * Conta le bozze
     */
    long countByPublishedFalse();
    
    /**
     * Trova bozze ordinate per data di creazione
     */
    Page<BlogPost> findByPublishedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Trova tutti i post ordinati per data di creazione (per dashboard)
     */
    List<BlogPost> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * Conta i post creati dopo una certa data
     */
    long countByCreatedAtAfter(LocalDateTime date);
}