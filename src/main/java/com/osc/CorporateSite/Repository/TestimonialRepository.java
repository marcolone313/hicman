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

    List<Testimonial> findByPublishedTrueOrderByDisplayOrderAsc();

    long countByPublishedTrue();

    Optional<Testimonial> findByIdAndPublishedTrue(Long id);

    @Query("SELECT t FROM Testimonial t WHERE t.published = true " +
           "ORDER BY t.displayOrder ASC")
    List<Testimonial> findTopPublishedTestimonials(int limit);

    // ==================== QUERY PER ADMIN ====================

    List<Testimonial> findAllByOrderByDisplayOrderAsc();

    List<Testimonial> findByPublishedFalseOrderByCreatedAtDesc();

    long countByPublishedFalse();

    List<Testimonial> findTop10ByOrderByCreatedAtDesc();

    // ==================== QUERY PER ORDINAMENTO ====================

    @Query("SELECT MAX(t.displayOrder) FROM Testimonial t")
    Integer findMaxDisplayOrder();

    Optional<Testimonial> findFirstByDisplayOrderLessThanOrderByDisplayOrderDesc(Integer displayOrder);

    Optional<Testimonial> findFirstByDisplayOrderGreaterThanOrderByDisplayOrderAsc(Integer displayOrder);

    Optional<Testimonial> findByDisplayOrder(Integer displayOrder);

    List<Testimonial> findByDisplayOrderGreaterThanOrderByDisplayOrderAsc(Integer displayOrder);

    List<Testimonial> findByDisplayOrderLessThanOrderByDisplayOrderDesc(Integer displayOrder);

    // ==================== QUERY DI RICERCA ====================

    List<Testimonial> findByAuthorContainingIgnoreCaseOrderByDisplayOrderAsc(String author);

    List<Testimonial> findByCompanyNameContainingIgnoreCaseOrderByDisplayOrderAsc(String companyName);

    List<Testimonial> findByAuthorRoleContainingIgnoreCaseOrderByDisplayOrderAsc(String authorRole);

    @Query("SELECT t FROM Testimonial t WHERE " +
           "LOWER(t.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.authorRole) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY t.displayOrder ASC")
    List<Testimonial> searchTestimonials(@Param("keyword") String keyword);

    /**
     * ✅ Corretto: rimossa parentesi in più
     */
    @Query("SELECT t FROM Testimonial t WHERE t.published = true AND " +
           "(LOWER(t.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.authorRole) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +  // ← una parentesi chiusa sola
           "ORDER BY t.displayOrder ASC")
    List<Testimonial> searchPublishedTestimonials(@Param("keyword") String keyword);

    // ==================== QUERY AVANZATE ====================

    List<Testimonial> findByCompanyNameAndPublishedTrueOrderByDisplayOrderAsc(String companyName);

    List<Testimonial> findBySourceUrlIsNotNullAndPublishedTrueOrderByDisplayOrderAsc();

    List<Testimonial> findBySourceUrlIsNullAndPublishedTrueOrderByDisplayOrderAsc();

    boolean existsByAuthorAndCompanyName(String author, String companyName);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Testimonial t " +
           "WHERE t.author = :author AND t.companyName = :companyName AND t.id != :id")
    boolean existsByAuthorAndCompanyNameAndIdNot(
            @Param("author") String author,
            @Param("companyName") String companyName,
            @Param("id") Long id);

    @Query("SELECT t.companyName, COUNT(t) FROM Testimonial t " +
           "WHERE t.published = true AND t.companyName IS NOT NULL " +
           "GROUP BY t.companyName ORDER BY COUNT(t) DESC")
    List<Object[]> countTestimonialsByCompany();

    @Query(value = "SELECT * FROM testimonials WHERE published = true ORDER BY RANDOM() LIMIT :limit",
           nativeQuery = true)
    List<Testimonial> findRandomPublishedTestimonials(@Param("limit") int limit);

    @Query("SELECT t FROM Testimonial t WHERE t.displayOrder BETWEEN :start AND :end " +
           "ORDER BY t.displayOrder ASC")
    List<Testimonial> findByDisplayOrderRange(@Param("start") int start, @Param("end") int end);

    @Query("SELECT t.author, COUNT(t) FROM Testimonial t " +
           "WHERE t.published = true " +
           "GROUP BY t.author ORDER BY COUNT(t) DESC")
    List<Object[]> countTestimonialsByAuthor();

    @Query("SELECT t FROM Testimonial t WHERE t.published = true " +
           "ORDER BY t.createdAt DESC")
    List<Testimonial> findRecentPublishedTestimonials();

    @Query("SELECT t FROM Testimonial t WHERE LENGTH(t.content) >= :minLength " +
           "AND t.published = true ORDER BY t.displayOrder ASC")
    List<Testimonial> findByMinContentLength(@Param("minLength") int minLength);

    @Query("SELECT t FROM Testimonial t WHERE LENGTH(t.content) <= :maxLength " +
           "AND t.published = true ORDER BY t.displayOrder ASC")
    List<Testimonial> findByMaxContentLength(@Param("maxLength") int maxLength);

    @Query("SELECT DISTINCT t.companyName FROM Testimonial t " +
           "WHERE t.companyName IS NOT NULL AND t.published = true " +
           "ORDER BY t.companyName ASC")
    List<String> findDistinctCompanyNames();

    @Query("SELECT DISTINCT t.author FROM Testimonial t " +
           "WHERE t.author IS NOT NULL AND t.published = true " +
           "ORDER BY t.author ASC")
    List<String> findDistinctAuthors();

    @Query("SELECT DISTINCT t.authorRole FROM Testimonial t " +
           "WHERE t.authorRole IS NOT NULL AND t.published = true " +
           "ORDER BY t.authorRole ASC")
    List<String> findDistinctAuthorRoles();

    @Query("UPDATE Testimonial t SET t.displayOrder = t.displayOrder + 1 " +
           "WHERE t.displayOrder >= :startOrder")
    void incrementDisplayOrderFrom(@Param("startOrder") int startOrder);

    @Query("UPDATE Testimonial t SET t.displayOrder = t.displayOrder - 1 " +
           "WHERE t.displayOrder > :startOrder")
    void decrementDisplayOrderFrom(@Param("startOrder") int startOrder);
}
