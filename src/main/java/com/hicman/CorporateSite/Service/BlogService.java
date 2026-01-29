package com.hicman.CorporateSite.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hicman.CorporateSite.Model.BlogPost;
import com.hicman.CorporateSite.Repository.BlogPostRepository;

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
     * Ottiene gli ultimi N post (tutti, anche bozze) per dashboard
     */
    public List<BlogPost> getLatestPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return blogPostRepository.findAll(pageable).getContent();
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
        
        return blogPostRepository.save(post);
    }

    /**
     * Salva un post con upload immagine
     */
    public BlogPost savePostWithImage(BlogPost post, MultipartFile imageFile) throws IOException {
        // Se c'è un file immagine, caricalo
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(imageFile, "blog");
            post.setImageUrl(imageUrl);
        }
        
        return savePost(post);
    }

    /**
     * Salva solo l'immagine e restituisce l'URL
     */
    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File immagine vuoto");
        }
        return fileStorageService.storeFile(file, "blog");
    }

    /**
     * Elimina un post per ID
     */
    public void deletePost(Long id) {
        if (!blogPostRepository.existsById(id)) {
            throw new IllegalArgumentException("Post non trovato con ID: " + id);
        }
        blogPostRepository.deleteById(id);
    }

    /**
     * Elimina multipli post
     */
    @Transactional
    public int deletePosts(Long[] postIds) {
        int count = 0;
        for (Long id : postIds) {
            if (blogPostRepository.existsById(id)) {
                blogPostRepository.deleteById(id);
                count++;
            }
        }
        return count;
    }

    /**
     * Elimina l'immagine associata ad un post
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            fileStorageService.deleteFile(imageUrl);
        }
    }

    /**
     * Pubblica o depubblica un post
     */
    public BlogPost togglePublish(Long id) {
        Optional<BlogPost> postOpt = blogPostRepository.findById(id);
        
        if (postOpt.isEmpty()) {
            throw new IllegalArgumentException("Post non trovato con ID: " + id);
        }
        
        BlogPost post = postOpt.get();
        post.setPublished(!post.isPublished());
        
        // Se viene pubblicato e non ha data di pubblicazione, impostala
        if (post.isPublished() && post.getPublishedDate() == null) {
            post.setPublishedDate(LocalDateTime.now());
        }
        
        return blogPostRepository.save(post);
    }

    // ==================== STATISTICHE ====================

    /**
     * Conta tutti i post (pubblicati e bozze)
     */
    public long countAllPosts() {
        return blogPostRepository.count();
    }

    /**
     * Conta solo le bozze
     */
    public long countDraftPosts() {
        return blogPostRepository.countByPublishedFalse();
    }

    /**
     * Conta i post recenti (ultimi N giorni)
     */
    public long countRecentPosts(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return blogPostRepository.countByCreatedAtAfter(sinceDate);
    }

    /**
     * Verifica se un post esiste
     */
    public boolean existsById(Long id) {
        return blogPostRepository.existsById(id);
    }
}