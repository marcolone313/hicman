package com.hicman.CorporateSite.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hicman.CorporateSite.Model.BlogPost;
import com.hicman.CorporateSite.Service.BlogService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/admin/blog")
public class AdminBlogController {

    @Autowired
    private BlogService blogService;

    /**
     * Lista tutti i post (pubblicati e bozze) per l'admin
     */
    @GetMapping
    public String listAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status,
            Model model) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BlogPost> blogPage;

        if ("published".equals(status)) {
            blogPage = blogService.getPublishedPosts(pageable);
        } else if ("draft".equals(status)) {
            blogPage = blogService.getDraftPosts(pageable);
        } else {
            blogPage = blogService.getAllPosts(pageable);
        }

        model.addAttribute("posts", blogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        model.addAttribute("totalItems", blogPage.getTotalElements());
        model.addAttribute("status", status);
        model.addAttribute("pageTitle", "Gestione Blog");

        return "admin/blog/list";
    }

    /**
     * Form per creare un nuovo post
     */
    @GetMapping("/new")
    public String newPostForm(Model model) {
        BlogPost post = new BlogPost();
        // Imposta data di pubblicazione di default (puoi rimuoverlo se non la vuoi precompilata)
        post.setPublishedDate(LocalDateTime.now());

        model.addAttribute("post", post);
        model.addAttribute("pageTitle", "Nuovo Articolo");
        model.addAttribute("isEdit", false);
        return "admin/blog/form";
    }

    /**
     * Form per modificare un post esistente
     */
    @GetMapping("/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<BlogPost> post = blogService.getPostById(id);

        if (post.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Articolo non trovato");
            return "redirect:/admin/blog";
        }

        model.addAttribute("post", post.get());
        model.addAttribute("pageTitle", "Modifica Articolo");
        model.addAttribute("isEdit", true);
        return "admin/blog/form";
    }

    /**
     * Salvataggio nuovo post o aggiornamento
     */
    @PostMapping("/save")
    public String savePost(
            @Valid @ModelAttribute("post") BlogPost post,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "removeImage", required = false) String removeImage,
            @RequestParam(value = "action", defaultValue = "save") String action,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validazione
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Correggi gli errori nel form");
            model.addAttribute("pageTitle", post.getId() == null ? "Nuovo Articolo" : "Modifica Articolo");
            model.addAttribute("isEdit", post.getId() != null);
            return "admin/blog/form";
        }
        System.out.println("DEBUG BLOG SAVE -> id=" + post.getId()
        + " published=" + post.isPublished()
        + " action=" + action
        + " publishedDate=" + post.getPublishedDate());


        try {
            // ===== GESTIONE RIMOZIONE IMMAGINE =====
            if ("true".equals(removeImage) && post.getImageUrl() != null) {
                blogService.deleteImage(post.getImageUrl());
                post.setImageUrl(null);
            }

            // ===== GESTIONE UPLOAD NUOVA IMMAGINE =====
            if (imageFile != null && !imageFile.isEmpty()) {
                if (!isValidImageFile(imageFile)) {
                    model.addAttribute("errorMessage",
                            "Formato immagine non valido. Sono accettati: JPG, PNG, WEBP (max 10MB)");
                    model.addAttribute("pageTitle", post.getId() == null ? "Nuovo Articolo" : "Modifica Articolo");
                    model.addAttribute("isEdit", post.getId() != null);
                    return "admin/blog/form";
                }

                // Se c'era già un'immagine, eliminala
                if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                    blogService.deleteImage(post.getImageUrl());
                }

                String imageUrl = blogService.saveImage(imageFile);
                post.setImageUrl(imageUrl);
            }

            // ===== GESTIONE PUBBLICAZIONE =====
            boolean isNew = (post.getId() == null);

            // Se premo "Salva e Pubblica", forzo la pubblicazione
            if ("save_and_publish".equals(action)) {
                post.setPublished(true);
            }
            // Altrimenti, con "Salva", il valore di post.isPublished()
            // viene dal checkbox th:field="*{published}" nel form.

            // Se è pubblicato e non ha data di pubblicazione, metto ora
            if (post.isPublished() && post.getPublishedDate() == null) {
                post.setPublishedDate(LocalDateTime.now());
            }

            // (Opzionale) se non è pubblicato, puoi azzerare la data:
            // if (!post.isPublished()) {
            //     post.setPublishedDate(null);
            // }

            // ===== SALVA IL POST =====
            BlogPost savedPost = blogService.savePost(post);

            // Messaggio di successo
            String message = isNew ? "Articolo creato" : "Articolo aggiornato";
            if (post.isPublished()) {
                message += " e pubblicato";
            }
            message += " con successo!";

            redirectAttributes.addFlashAttribute("successMessage", message);

            return "redirect:/admin/blog/edit/" + savedPost.getId();

        } catch (IOException e) {
            model.addAttribute("errorMessage",
                    "Errore durante il salvataggio dell'immagine: " + e.getMessage());
            model.addAttribute("pageTitle", post.getId() == null ? "Nuovo Articolo" : "Modifica Articolo");
            model.addAttribute("isEdit", post.getId() != null);
            return "admin/blog/form";

        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    "Errore durante il salvataggio: " + e.getMessage());
            model.addAttribute("pageTitle", post.getId() == null ? "Nuovo Articolo" : "Modifica Articolo");
            model.addAttribute("isEdit", post.getId() != null);
            return "admin/blog/form";
        }
    }

    /**
     * Pubblicazione/Depubblicazione rapida dalla lista
     */
    @PostMapping("/toggle-publish/{id}")
    public String togglePublish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<BlogPost> postOpt = blogService.getPostById(id);

            if (postOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Articolo non trovato");
                return "redirect:/admin/blog";
            }

            BlogPost post = postOpt.get();
            post.setPublished(!post.isPublished());

            if (post.isPublished() && post.getPublishedDate() == null) {
                post.setPublishedDate(LocalDateTime.now());
            }

            blogService.savePost(post);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Articolo " + (post.isPublished() ? "pubblicato" : "rimosso dalla pubblicazione")
                            + " con successo!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Errore: " + e.getMessage());
        }

        return "redirect:/admin/blog";
    }

    /**
     * Cancellazione singolo post
     */
    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<BlogPost> post = blogService.getPostById(id);

            if (post.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Articolo non trovato");
                return "redirect:/admin/blog";
            }

            if (post.get().getImageUrl() != null && !post.get().getImageUrl().isEmpty()) {
                blogService.deleteImage(post.get().getImageUrl());
            }

            blogService.deletePost(id);
            redirectAttributes.addFlashAttribute("successMessage", "Articolo eliminato con successo!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Errore durante l'eliminazione: " + e.getMessage());
        }

        return "redirect:/admin/blog";
    }

    /**
     * Eliminazione multipla post
     */
    @PostMapping("/delete-multiple")
    public String deleteMultiplePosts(
            @RequestParam("postIds") Long[] postIds,
            RedirectAttributes redirectAttributes) {

        try {
            if (postIds == null || postIds.length == 0) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Nessun articolo selezionato");
                return "redirect:/admin/blog";
            }

            for (Long id : postIds) {
                Optional<BlogPost> post = blogService.getPostById(id);
                if (post.isPresent() && post.get().getImageUrl() != null) {
                    blogService.deleteImage(post.get().getImageUrl());
                }
            }

            int deletedCount = blogService.deletePosts(postIds);
            redirectAttributes.addFlashAttribute("successMessage",
                    deletedCount + " articolo/i eliminato/i con successo!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Errore durante l'eliminazione: " + e.getMessage());
        }

        return "redirect:/admin/blog";
    }

    /**
     * Anteprima post (reindirizza alla pagina pubblica)
     */
    @GetMapping("/preview/{id}")
    public String previewPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<BlogPost> post = blogService.getPostById(id);

        if (post.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Articolo non trovato");
            return "redirect:/admin/blog";
        }

        return "redirect:/rassegna-stampa/" + id;
    }

    // ==================== UTILITY METHODS ====================

    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/jpg");
    }
}
