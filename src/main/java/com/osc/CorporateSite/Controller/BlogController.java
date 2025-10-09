package com.osc.CorporateSite.Controller;

import com.osc.CorporateSite.Model.BlogPost;
import com.osc.CorporateSite.Model.Testimonial;
import com.osc.CorporateSite.Service.BlogService;
import com.osc.CorporateSite.Service.TestimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private TestimonialService testimonialService;

    /**
     * Lista di tutti i post pubblicati con paginazione
     */
    @GetMapping
    public String listBlogPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPost> blogPage = blogService.getPublishedPosts(pageable);
        
        model.addAttribute("posts", blogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        model.addAttribute("totalItems", blogPage.getTotalElements());
        model.addAttribute("pageTitle", "Rassegna Stampa");
        
        return "blog/list";
    }

    /**
     * Dettaglio singolo post
     */
    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Optional<BlogPost> post = blogService.getPublishedPostById(id);
        
        if (post.isEmpty()) {
            return "redirect:/blog";
        }
        
        model.addAttribute("post", post.get());
        model.addAttribute("pageTitle", post.get().getTitle());
        
        // Post correlati (ultimi 3 post escluso quello corrente)
        List<BlogPost> relatedPosts = blogService.getLatestPublishedPosts(4)
            .stream()
            .filter(p -> !p.getId().equals(id))
            .limit(3)
            .toList();
        model.addAttribute("relatedPosts", relatedPosts);
        
        return "blog/detail";
    }

    /**
     * Pagina "Dicono di Noi" con testimonial
     */
    @GetMapping("/testimonials")
    public String listTestimonials(Model model) {
        List<Testimonial> testimonials = testimonialService.getPublishedTestimonials();
        
        model.addAttribute("testimonials", testimonials);
        model.addAttribute("pageTitle", "Dicono di Noi");
        
        return "blog/testimonials";
    }

    /**
     * Ricerca nei post del blog
     */
    @GetMapping("/search")
    public String searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPost> searchResults = blogService.searchPublishedPosts(query, pageable);
        
        model.addAttribute("posts", searchResults.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", searchResults.getTotalPages());
        model.addAttribute("totalItems", searchResults.getTotalElements());
        model.addAttribute("query", query);
        model.addAttribute("pageTitle", "Risultati ricerca: " + query);
        
        return "blog/list";
    }
}