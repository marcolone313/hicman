package com.osc.CorporateSite.Service;

import com.osc.CorporateSite.Model.BlogPost;
import com.osc.CorporateSite.Repository.BlogPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BlogService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // ==================== METODI PUBBLICI ====================

    /**
     * Ottiene tutti i post pubblicati con paginazione
     */
    public Page<BlogPost> getPublishedPosts(Pageable pageable) {
        return blogPostRepository.findByPublishedTrueOrderByPublishedDateDesc(pageable);
    }

    /**
     * Ottiene un post pubblicato per ID
     */
    public Optional<BlogPost> getPublishedPostById(Long id) {
        return blogPostRepository.findByIdAndPublishedTrue(id);
    }

    /**
     * Ottiene gli ultimi N post pubblicati
     */
    public List<BlogPost> getLatestPublishedPosts(int limit) {
        return blogPostRepository.findTop10ByPublishedTrueOrderByPublishedDateDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Ricerca nei post pubblicati
     */
    public Page<BlogPost> searchPublishedPosts(String query, Pageable pageable) {
        return blogPostRepository.searchPublishedPosts(query, pageable);
    }

    /**
     * Conta i post pubblicati
     */
    public long countPublishedPosts() {
        return blogPostRepository.countByPublishedTrue();
    }

    // ==================== METODI ADMIN ====================

    /**
     * Ottiene tutti i post (pubblicati e bozze) con paginazione
     */
    public Page<BlogPost> getAllPosts(Pageable pageable) {
        return blogPostRepository.findAll(pageable);
    }

    /**
     * Ottiene tutti i post in bozza
     */
    public Page<BlogPost> getDraftPosts(Pageable pageable) {
        return blogPostRepository.findByPublishedFalseOrderByCreatedAtDesc(pageable);
    }

    /**
     * Ottiene un post per ID (anche se non pubblicato)
     */
    public Optional<BlogPost> getPostById(Long id) {
        return blogPostRepository.findById(id);
    }

    /**
     * Ottiene gli ultimi N post (tutti)
     */
    public List<BlogPost> getLatestPosts(int limit) {
        return blogPostRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Salva un nuovo post o aggiorna uno esistente
     */
    public BlogPost savePost(BlogPost post) {
        // Se è un nuovo post, imposta la data di creazione
        if (post.getId() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        
        // Aggiorna sempre la data di modifica
        post.setUpdatedAt(LocalDateTime.now());
        
        // Se il post viene pubblicato e non ha ancora una data di pubblicazione
        if (post.isPublished() && post.getPublishedDate() == null) {
            post.setPublishedDate(LocalDateTime.now());
        }
        
        // Se il post viene depubblicato, mantieni la data di pubblicazione originale
        // ma puoi decidere di azzerarla se preferisci
        // if (!post.isPublished()) {
        //     post.setPublishedDate(null);
        // }
        
        return blogPostRepository.save(post);
    }

    /**
     * Elimina un post
     */
    public void deletePost(Long id) {
        blogPostRepository.deleteById(id);
    }

    /**
     * Elimina più post contemporaneamente
     */
    public int deletePosts(Long[] postIds) {
        int count = 0;
        for (Long id : postIds) {
            try {
                blogPostRepository.deleteById(id);
                count++;
            } catch (Exception e) {
                // Log error ma continua con gli altri
                System.err.println("Errore eliminazione post ID " + id + ": " + e.getMessage());
            }
        }
        return count;
    }

    /**
     * Conta tutti i post
     */
    public long countAllPosts() {
        return blogPostRepository.count();
    }

    /**
     * Conta i post in bozza
     */
    public long countDraftPosts() {
        return blogPostRepository.countByPublishedFalse();
    }

    /**
     * Cambia lo stato di pubblicazione di un post
     */
    public BlogPost togglePublishStatus(Long id) {
        Optional<BlogPost> postOpt = blogPostRepository.findById(id);
        
        if (postOpt.isEmpty()) {
            throw new IllegalArgumentException("Post non trovato con ID: " + id);
        }
        
        BlogPost post = postOpt.get();
        post.setPublished(!post.isPublished());
        
        if (post.isPublished() && post.getPublishedDate() == null) {
            post.setPublishedDate(LocalDateTime.now());
        }
        
        return blogPostRepository.save(post);
    }

    // ==================== GESTIONE IMMAGINI ====================

    /**
     * Salva un'immagine e restituisce l'URL
     */
    public String saveImage(MultipartFile file) throws IOException {
        return fileStorageService.storeFile(file, "blog");
    }

    /**
     * Elimina un'immagine
     */
    public void deleteImage(String imageUrl) {
        fileStorageService.deleteFile(imageUrl);
    }

    // ==================== METODI DI UTILITY ====================

    /**
     * Verifica se un post esiste
     */
    public boolean existsById(Long id) {
        return blogPostRepository.existsById(id);
    }

    /**
     * Ottiene post per fonte/sorgente
     */
    public List<BlogPost> getPostsBySource(String sourceName) {
        return blogPostRepository.findBySourceNameOrderByPublishedDateDesc(sourceName);
    }

    /**
     * Ottiene post pubblicati in un range di date
     */
    public List<BlogPost> getPostsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return blogPostRepository.findByPublishedDateBetweenAndPublishedTrueOrderByPublishedDateDesc(
                startDate, endDate);
    }

    /**
     * Ottiene statistiche mensili
     */
    public long countPostsInMonth(int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);
        return blogPostRepository.countByPublishedDateBetweenAndPublishedTrue(startDate, endDate);
    }
}