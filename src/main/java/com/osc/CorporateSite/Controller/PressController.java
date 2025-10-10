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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Controller per la Rassegna Stampa e Dicono di Noi
 */
@Controller
public class PressController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private TestimonialService testimonialService;

    /**
     * Pagina principale Rassegna Stampa
     */
    @GetMapping("/rassegna-stampa")
    public String rassegnaStampa(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPost> blogPage = blogService.getPublishedPosts(pageable);
        
        model.addAttribute("posts", blogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        model.addAttribute("totalItems", blogPage.getTotalElements());
        model.addAttribute("pageTitle", "Hicman Capital Partner - Rassegna Stampa");
        
        return "rassegna-stampa";
    }

    /**
     * Dettaglio singolo articolo
     */
    @GetMapping("/rassegna-stampa/{id}")
    public String viewArticle(@PathVariable Long id, Model model) {
        Optional<BlogPost> post = blogService.getPublishedPostById(id);
        
        if (post.isEmpty()) {
            return "redirect:/rassegna-stampa";
        }
        
        model.addAttribute("post", post.get());
        model.addAttribute("pageTitle", post.get().getTitle() + " - Hicman Capital Partner");
        
        // Post correlati (ultimi 3 post escluso quello corrente)
        List<BlogPost> relatedPosts = blogService.getLatestPublishedPosts(4)
            .stream()
            .filter(p -> !p.getId().equals(id))
            .limit(3)
            .toList();
        model.addAttribute("relatedPosts", relatedPosts);
        
        return "press-detail";
    }

    /**
     * Pagina "Dicono di Noi" con testimonial
     */
    @GetMapping("/dicono-di-noi")
    public String diconoDiNoi(Model model) {
        List<Testimonial> testimonials = testimonialService.getPublishedTestimonials();
        
        model.addAttribute("testimonials", testimonials);
        model.addAttribute("pageTitle", "Hicman Capital Partner - Dicono di Noi");
        
        return "dicono-di-noi";
    }
}