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

    // ==================== QUERY PER LATO PUBBLICO ====================

    /**
     * Trova tutti i post pubblicati ordinati per data di pubblicazione (paginati)
     */
    Page<BlogPost> findByPublishedTrueOrderByPublishedDateDesc(Pageable pageable);

    /**
     * Trova un post pubblicato per ID
     */
    Optional<BlogPost> findByIdAndPublishedTrue(Long id);

    /**
     * Trova i primi 10 post pubblicati più recenti
     */
    List<BlogPost> findTop10ByPublishedTrueOrderByPublishedDateDesc();

    /**
     * Conta i post pubblicati
     */
    long countByPublishedTrue();

    /**
     * Ricerca fulltext nei post pubblicati (titolo e contenuto)
     */
    @Query("SELECT p FROM BlogPost p WHERE p.published = true " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.sourceName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY p.publishedDate DESC")
    Page<BlogPost> searchPublishedPosts(@Param("query") String query, Pageable pageable);

    /**
     * Trova post pubblicati per fonte/sorgente
     */
    List<BlogPost> findBySourceNameAndPublishedTrueOrderByPublishedDateDesc(String sourceName);

    /**
     * Trova post pubblicati in un range di date
     */
    List<BlogPost> findByPublishedDateBetweenAndPublishedTrueOrderByPublishedDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Conta post pubblicati in un range di date
     */
    long countByPublishedDateBetweenAndPublishedTrue(LocalDateTime startDate, LocalDateTime endDate);

    // ==================== QUERY PER ADMIN ====================

    /**
     * Trova tutti i post in bozza ordinati per data di creazione
     */
    Page<BlogPost> findByPublishedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Conta i post in bozza
     */
    long countByPublishedFalse();

    /**
     * Trova i primi 10 post più recenti (tutti)
     */
    List<BlogPost> findTop10ByOrderByCreatedAtDesc();

    /**
     * Trova tutti i post per fonte/sorgente (anche non pubblicati)
     */
    List<BlogPost> findBySourceNameOrderByPublishedDateDesc(String sourceName);

    /**
     * Ricerca fulltext in tutti i post (admin)
     */
    @Query("SELECT p FROM BlogPost p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.sourceName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<BlogPost> searchAllPosts(@Param("query") String query, Pageable pageable);

    // ==================== QUERY STATISTICHE ====================

    /**
     * Conta post per anno
     */
    @Query("SELECT COUNT(p) FROM BlogPost p WHERE " +
           "YEAR(p.publishedDate) = :year AND p.published = true")
    long countPostsByYear(@Param("year") int year);

    /**
     * Conta post per mese
     */
    @Query("SELECT COUNT(p) FROM BlogPost p WHERE " +
           "YEAR(p.publishedDate) = :year AND MONTH(p.publishedDate) = :month " +
           "AND p.published = true")
    long countPostsByMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Ottiene il numero di post per fonte
     */
    @Query("SELECT p.sourceName, COUNT(p) FROM BlogPost p WHERE p.published = true " +
           "GROUP BY p.sourceName ORDER BY COUNT(p) DESC")
    List<Object[]> countPostsBySource();

    /**
     * Ottiene i post più recenti per data di aggiornamento
     */
    List<BlogPost> findTop5ByOrderByUpdatedAtDesc();

    /**
     * Trova post con link esterno
     */
    List<BlogPost> findByExternalLinkIsNotNullAndPublishedTrueOrderByPublishedDateDesc();

    /**
     * Trova post senza link esterno (solo contenuto interno)
     */
    List<BlogPost> findByExternalLinkIsNullAndPublishedTrueOrderByPublishedDateDesc();

    // ==================== QUERY AVANZATE ====================

    /**
     * Trova post pubblicati dopo una certa data
     */
    List<BlogPost> findByPublishedDateAfterAndPublishedTrueOrderByPublishedDateDesc(LocalDateTime date);

    /**
     * Trova post pubblicati prima di una certa data
     */
    List<BlogPost> findByPublishedDateBeforeAndPublishedTrueOrderByPublishedDateDesc(LocalDateTime date);

    /**
     * Verifica se esiste un post con lo stesso titolo
     */
    boolean existsByTitleIgnoreCase(String title);

    /**
     * Verifica se esiste un post con lo stesso titolo (escludendo un ID specifico)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM BlogPost p " +
           "WHERE LOWER(p.title) = LOWER(:title) AND p.id != :id")
    boolean existsByTitleIgnoreCaseAndIdNot(@Param("title") String title, @Param("id") Long id);

    /**
     * Ottiene post correlati in base alla fonte
     */
    @Query("SELECT p FROM BlogPost p WHERE p.sourceName = :sourceName " +
           "AND p.id != :excludeId AND p.published = true " +
           "ORDER BY p.publishedDate DESC")
    List<BlogPost> findRelatedPostsBySource(
            @Param("sourceName") String sourceName,
            @Param("excludeId") Long excludeId,
            Pageable pageable);

    /**
     * Ottiene post correlati in base a parole chiave nel titolo
     */
    @Query("SELECT p FROM BlogPost p WHERE p.published = true " +
           "AND p.id != :excludeId " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.publishedDate DESC")
    List<BlogPost> findRelatedPostsByKeyword(
            @Param("keyword") String keyword,
            @Param("excludeId") Long excludeId,
            Pageable pageable);

    /**
     * Trova post per anno di pubblicazione
     */
    @Query("SELECT p FROM BlogPost p WHERE YEAR(p.publishedDate) = :year " +
           "AND p.published = true ORDER BY p.publishedDate DESC")
    List<BlogPost> findPostsByYear(@Param("year") int year);

    /**
     * Trova post per mese e anno di pubblicazione
     */
    @Query("SELECT p FROM BlogPost p WHERE YEAR(p.publishedDate) = :year " +
           "AND MONTH(p.publishedDate) = :month AND p.published = true " +
           "ORDER BY p.publishedDate DESC")
    List<BlogPost> findPostsByYearAndMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Ottiene archivio per anno/mese (per sidebar)
     */
    @Query("SELECT YEAR(p.publishedDate) as year, MONTH(p.publishedDate) as month, COUNT(p) as count " +
           "FROM BlogPost p WHERE p.published = true " +
           "GROUP BY YEAR(p.publishedDate), MONTH(p.publishedDate) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getArchiveByYearAndMonth();

    /**
     * Trova post con immagine
     */
    List<BlogPost> findByImageUrlIsNotNullAndPublishedTrueOrderByPublishedDateDesc();

    /**
     * Trova post senza immagine
     */
    List<BlogPost> findByImageUrlIsNullAndPublishedTrueOrderByPublishedDateDesc();

    /**
     * Ottiene le fonti più utilizzate
     */
    @Query("SELECT p.sourceName, COUNT(p) as count FROM BlogPost p " +
           "WHERE p.published = true AND p.sourceName IS NOT NULL " +
           "GROUP BY p.sourceName ORDER BY count DESC")
    List<Object[]> getTopSources(Pageable pageable);

    /**
     * Post più recenti pubblicati (con limite)
     */
    @Query("SELECT p FROM BlogPost p WHERE p.published = true " +
           "ORDER BY p.publishedDate DESC")
    List<BlogPost> findRecentPublishedPosts(Pageable pageable);

    /**
     * Elimina post più vecchi di una certa data
     */
    @Query("DELETE FROM BlogPost p WHERE p.publishedDate < :date")
    void deleteOldPosts(@Param("date") LocalDateTime date);
}