package com.osc.CorporateSite.Repository;

import com.osc.CorporateSite.Model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    // ==================== QUERY PER LATO PUBBLICO ====================

    /**
     * Trova tutti i testimonial pubblicati ordinati per displayOrder
     */
    List<Testimonial> findByPublishedTrueOrderByDisplayOrderAsc();

    /**
     * Conta i testimonial pubblicati
     */
    long countByPublishedTrue();

    /**
     * Trova un testimonial pubblicato per ID
     */
    Optional<Testimonial> findByIdAndPublishedTrue(Long id);

    /**
     * Trova i primi N testimonial pubblicati
     */
    @Query("SELECT t FROM Testimonial t WHERE t.published = true " +
           "ORDER BY t.displayOrder ASC")
    List<Testimonial> findTopPublishedTestimonials(int limit);

    // ==================== QUERY PER ADMIN ====================

    /**
     * Trova tutti i testimonial ordinati per displayOrder
     */
    List<Testimonial> findAllByOrderByDisplayOrderAsc();

    /**
     * Trova tutti i testimonial in bozza
     */
    List<Testimonial> findByPublishedFalseOrderByCreatedAtDesc();

    /**
     * Conta i testimonial in bozza
     */
    long countByPublishedFalse();

    /**
     * Trova i primi 10 testimonial più recenti
     */
    List<Testimonial> findTop10ByOrderByCreatedAtDesc();

    // ==================== QUERY PER ORDINAMENTO ====================

    /**
     * Trova il valore massimo di displayOrder
     */
    @Query("SELECT MAX(t.displayOrder) FROM Testimonial t")
    Integer findMaxDisplayOrder();

    /**
     * Trova il primo testimonial con displayOrder inferiore a quello dato
     */
    Optional<Testimonial> findFirstByDisplayOrderLessThanOrderByDisplayOrderDesc(Integer displayOrder);

    /**
     * Trova il primo testimonial con displayOrder superiore a quello dato
     */
    Optional<Testimonial> findFirstByDisplayOrderGreaterThanOrderByDisplayOrderAsc(Integer displayOrder);

    /**
     * Trova testimonial per displayOrder specifico
     */
    Optional<Testimonial> findByDisplayOrder(Integer displayOrder);

    /**
     * Trova tutti i testimonial con displayOrder maggiore di un valore
     */
    List<Testimonial> findByDisplayOrderGreaterThanOrderByDisplayOrderAsc(Integer displayOrder);

    /**
     * Trova tutti i testimonial con displayOrder minore di un valore
     */
    List<Testimonial> findByDisplayOrderLessThanOrderByDisplayOrderDesc(Integer displayOrder);

    // ==================== QUERY DI RICERCA ====================

    /**
     * Ricerca per autore (case insensitive)
     */
    List<Testimonial> findByAuthorContainingIgnoreCaseOrderByDisplayOrderAsc(String author);

    /**
     * Ricerca per nome azienda (case insensitive)
     */
    List<Testimonial> findByCompanyNameContainingIgnoreCaseOrderByDisplayOrderAsc(String companyName);

    /**
     * Ricerca per ruolo autore (case insensitive)
     */
    List<Testimonial> findByAuthorRoleContainingIgnoreCaseOrderByDisplayOrderAsc(String authorRole);

    /**
     * Ricerca fulltext in tutti i campi
     */
    @Query("SELECT t FROM Testimonial t WHERE " +
           "LOWER(t.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.authorRole) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY t.displayOrder ASC")
    List<Testimonial> searchTestimonials(@Param("keyword") String keyword);

    /**
     * Ricerca fulltext solo nei testimonial pubblicati
     */
    @Query("SELECT t FROM Testimonial t WHERE t.published = true AND " +
           "(LOWER(t.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.authorRole) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "ORDER BY t.displayOrder ASC")
    List<Testimonial> searchPublishedTestimonials(@Param("keyword") String keyword);

    // ==================== QUERY AVANZATE ====================

    /**
     * Trova testimonial per azienda (pubblicati)
     */
    List<Testimonial> findByCompanyNameAndPublishedTrueOrderByDisplayOrderAsc(String companyName);

    /**
     * Trova testimonial con URL sorgente
     */
    List<Testimonial> findBySourceUrlIsNotNullAndPublishedTrueOrderByDisplayOrderAsc();

    /**
     * Trova testimonial senza URL sorgente
     */
    List<Testimonial> findBySourceUrlIsNullAndPublishedTrueOrderByDisplayOrderAsc();

    /**
     * Verifica se esiste un testimonial con lo stesso autore e azienda
     */
    boolean existsByAuthorAndCompanyName(String author, String companyName);

    /**
     * Verifica se esiste un testimonial con lo stesso autore e azienda (escludendo un ID)
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Testimonial t " +
           "WHERE t.author = :author AND t.companyName = :companyName AND t.id != :id")
    boolean existsByAuthorAndCompanyNameAndIdNot(
            @Param("author") String author,
            @Param("companyName") String companyName,
            @Param("id") Long id);

    /**
     * Ottiene statistiche per azienda
     */
    @Query("SELECT t.companyName, COUNT(t) FROM Testimonial t " +
           "WHERE t.published = true AND t.companyName IS NOT NULL " +
           "GROUP BY t.companyName ORDER BY COUNT(t) DESC")
    List<Object[]> countTestimonialsByCompany();

    /**
     * Ottiene testimonial casuali (per rotazione)
     */
    @Query(value = "SELECT * FROM testimonials WHERE published = true ORDER BY RANDOM() LIMIT :limit",
           nativeQuery = true)
    List<Testimonial> findRandomPublishedTestimonials(@Param("limit") int limit);

    /**
     * Trova testimonial per range di displayOrder
     */
    @Query("SELECT t FROM Testimonial t WHERE t.displayOrder BETWEEN :start AND :end " +
           "ORDER BY t.displayOrder ASC")
    List<Testimonial> findByDisplayOrderRange(@Param("start") int start, @Param("end") int end);

    /**
     * Ottiene il conteggio di testimonial per autore
     */
    @Query("SELECT t.author, COUNT(t) FROM Testimonial t " +
           "WHERE t.published = true " +
           "GROUP BY t.author ORDER BY COUNT(t) DESC")
    List<Object[]> countTestimonialsByAuthor();

    /**
     * Trova testimonial più recenti pubblicati
     */
    @Query("SELECT t FROM Testimonial t WHERE t.published = true " +
           "ORDER BY t.createdAt DESC")
    List<Testimonial> findRecentPublishedTestimonials();

    /**
     * Trova testimonial con contenuto più lungo di N caratteri
     */
    @Query("SELECT t FROM Testimonial t WHERE LENGTH(t.content) >= :minLength " +
           "AND t.published = true ORDER BY t.displayOrder ASC")
    List<Testimonial> findByMinContentLength(@Param("minLength") int minLength);

    /**
     * Trova testimonial con contenuto più corto di N caratteri
     */
    @Query("SELECT t FROM Testimonial t WHERE LENGTH(t.content) <= :maxLength " +
           "AND t.published = true ORDER BY t.displayOrder ASC")
    List<Testimonial> findByMaxContentLength(@Param("maxLength") int maxLength);

    /**
     * Ottiene lista delle aziende uniche (per filtri)
     */
    @Query("SELECT DISTINCT t.companyName FROM Testimonial t " +
           "WHERE t.companyName IS NOT NULL AND t.published = true " +
           "ORDER BY t.companyName ASC")
    List<String> findDistinctCompanyNames();

    /**
     * Ottiene lista degli autori unici (per filtri)
     */
    @Query("SELECT DISTINCT t.author FROM Testimonial t " +
           "WHERE t.author IS NOT NULL AND t.published = true " +
           "ORDER BY t.author ASC")
    List<String> findDistinctAuthors();

    /**
     * Ottiene lista dei ruoli unici (per filtri)
     */
    @Query("SELECT DISTINCT t.authorRole FROM Testimonial t " +
           "WHERE t.authorRole IS NOT NULL AND t.published = true " +
           "ORDER BY t.authorRole ASC")
    List<String> findDistinctAuthorRoles();

    /**
     * Riordina tutti i testimonial in sequenza
     */
    @Query("UPDATE Testimonial t SET t.displayOrder = t.displayOrder + 1 " +
           "WHERE t.displayOrder >= :startOrder")
    void incrementDisplayOrderFrom(@Param("startOrder") int startOrder);

    /**
     * Decrementa displayOrder da una certa posizione
     */
    @Query("UPDATE Testimonial t SET t.displayOrder = t.displayOrder - 1 " +
           "WHERE t.displayOrder > :startOrder")
    void decrementDisplayOrderFrom(@Param("startOrder") int startOrder);
}