package com.osc.CorporateSite.Controller;

import com.osc.CorporateSite.Model.BlogPost;
import com.osc.CorporateSite.Service.BlogService;
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
        model.addAttribute("post", new BlogPost());
        model.addAttribute("pageTitle", "Nuovo Post");
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
            redirectAttributes.addFlashAttribute("errorMessage", "Post non trovato");
            return "redirect:/admin/blog";
        }
        
        model.addAttribute("post", post.get());
        model.addAttribute("pageTitle", "Modifica Post");
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
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(defaultValue = "false") boolean publish,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", post.getId() == null ? "Nuovo Post" : "Modifica Post");
            model.addAttribute("isEdit", post.getId() != null);
            return "admin/blog/form";
        }
        
        try {
            // Gestione upload immagine
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = blogService.saveImage(imageFile);
                post.setImageUrl(imageUrl);
            }
            
            // Imposta lo stato di pubblicazione
            post.setPublished(publish);
            if (publish && post.getPublishedDate() == null) {
                post.setPublishedDate(LocalDateTime.now());
            }
            
            BlogPost savedPost = blogService.savePost(post);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Post " + (post.getId() == null ? "creato" : "aggiornato") + " con successo!");
            
            return "redirect:/admin/blog/edit/" + savedPost.getId();
            
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Errore durante il salvataggio dell'immagine: " + e.getMessage());
            model.addAttribute("pageTitle", post.getId() == null ? "Nuovo Post" : "Modifica Post");
            model.addAttribute("isEdit", post.getId() != null);
            return "admin/blog/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante il salvataggio: " + e.getMessage());
            model.addAttribute("pageTitle", post.getId() == null ? "Nuovo Post" : "Modifica Post");
            model.addAttribute("isEdit", post.getId() != null);
            return "admin/blog/form";
        }
    }

    /**
     * Pubblicazione/Depubblicazione rapida
     */
    @PostMapping("/toggle-publish/{id}")
    public String togglePublish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<BlogPost> postOpt = blogService.getPostById(id);
            
            if (postOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Post non trovato");
                return "redirect:/admin/blog";
            }
            
            BlogPost post = postOpt.get();
            post.setPublished(!post.isPublished());
            
            if (post.isPublished() && post.getPublishedDate() == null) {
                post.setPublishedDate(LocalDateTime.now());
            }
            
            blogService.savePost(post);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Post " + (post.isPublished() ? "pubblicato" : "rimosso dalla pubblicazione") + " con successo!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
        
        return "redirect:/admin/blog";
    }

    /**
     * Cancellazione post
     */
    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<BlogPost> post = blogService.getPostById(id);
            
            if (post.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Post non trovato");
                return "redirect:/admin/blog";
            }
            
            // Elimina anche l'immagine associata se esiste
            if (post.get().getImageUrl() != null) {
                blogService.deleteImage(post.get().getImageUrl());
            }
            
            blogService.deletePost(id);
            redirectAttributes.addFlashAttribute("successMessage", "Post eliminato con successo!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore durante l'eliminazione: " + e.getMessage());
        }
        
        return "redirect:/admin/blog";
    }

    /**
     * Anteprima post
     */
    @GetMapping("/preview/{id}")
    public String previewPost(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<BlogPost> post = blogService.getPostById(id);
        
        if (post.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Post non trovato");
            return "redirect:/admin/blog";
        }
        
        model.addAttribute("post", post.get());
        model.addAttribute("preview", true);
        model.addAttribute("pageTitle", "Anteprima: " + post.get().getTitle());
        
        return "blog/detail";
    }

    /**
     * Eliminazione multipla
     */
    @PostMapping("/delete-multiple")
    public String deleteMultiplePosts(
            @RequestParam("postIds") Long[] postIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            int deletedCount = blogService.deletePosts(postIds);
            redirectAttributes.addFlashAttribute("successMessage", 
                deletedCount + " post eliminati con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Errore durante l'eliminazione: " + e.getMessage());
        }
        
        return "redirect:/admin/blog";
    }
}