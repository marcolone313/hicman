package com.hicman.CorporateSite.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.hicman.CorporateSite.Model.BlogPost;
import com.hicman.CorporateSite.Model.Testimonial;
import com.hicman.CorporateSite.Service.BlogService;
import com.hicman.CorporateSite.Service.TestimonialService;

import java.util.List;
import java.util.Optional;

/**
 * Controller per la Rassegna Stampa e Dicono di Noi
 * Gestisce le pagine pubbliche della sezione Press
 */
@Controller
public class PressController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private TestimonialService testimonialService;

    /**
     * Pagina principale Rassegna Stampa
     * Lista paginata di tutti gli articoli pubblicati
     * 
     * @param page Numero della pagina (default: 0)
     * @param size Elementi per pagina (default: 9)
     * @param model Model per Thymeleaf
     * @return Template rassegna-stampa.html
     */
    @GetMapping("/rassegna-stampa")
    public String rassegnaStampa(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        
        // Validazione parametri
        if (page < 0) page = 0;
        if (size < 1) size = 9;
        if (size > 50) size = 50; // Max 50 items per page
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPost> blogPage = blogService.getPublishedPosts(pageable);
        
        // Aggiungi attributi al model
        model.addAttribute("posts", blogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        model.addAttribute("totalItems", blogPage.getTotalElements());
        model.addAttribute("pageTitle", "Hicman Capital Partner - Rassegna Stampa");
        
        return "rassegna-stampa";
    }

    /**
     * Dettaglio singolo articolo
     * 
     * @param id ID dell'articolo
     * @param model Model per Thymeleaf
     * @return Template press-detail.html o redirect se non trovato
     */
    @GetMapping("/rassegna-stampa/{id}")
    public String viewArticle(@PathVariable Long id, Model model) {
        Optional<BlogPost> postOpt = blogService.getPublishedPostById(id);
        
        if (postOpt.isEmpty()) {
            // Articolo non trovato o non pubblicato
            return "redirect:/rassegna-stampa";
        }
        
        BlogPost post = postOpt.get();
        model.addAttribute("post", post);
        model.addAttribute("pageTitle", post.getTitle() + " - Hicman Capital Partner");
        
        // Carica articoli correlati (ultimi 3 post escluso quello corrente)
        List<BlogPost> relatedPosts = blogService.getLatestPublishedPosts(4)
            .stream()
            .filter(p -> !p.getId().equals(id))
            .limit(3)
            .toList();
        model.addAttribute("relatedPosts", relatedPosts);
        
        return "press-detail";
    }

    /**
     * Redirect da /blog/{id} a /rassegna-stampa/{id} per compatibilità
     */
    /*@GetMapping("/blog/{id}")
    public String blogDetailRedirect(@PathVariable String id) {
        try {
            Long postId = Long.parseLong(id);
            return "redirect:/rassegna-stampa/" + postId;
        } catch (NumberFormatException e) {
            // Se non è un numero, redirect alla home o pagina non trovata
            return "redirect:/rassegna-stampa";
        }
    }*/

    /**
     * Pagina "Dicono di Noi" - Testimonials
     * Lista di tutte le testimonianze pubblicate
     * 
     * @param model Model per Thymeleaf
     * @return Template dicono-di-noi.html
     */
    @GetMapping("/dicono-di-noi")
    public String diconoDiNoi(Model model) {
        // Carica tutte le testimonianze pubblicate (ordinate per data desc)
        List<Testimonial> testimonials = testimonialService.getAllPublishedTestimonials();
        
        model.addAttribute("testimonials", testimonials);
        model.addAttribute("pageTitle", "Hicman Capital Partner - Dicono di Noi");
        
        return "dicono-di-noi";
    }

    /**
     * Dettaglio singola testimonianza (opzionale, se serve una pagina dedicata)
     * Altrimenti le testimonianze sono mostrate tutte in una singola pagina
     */
    @GetMapping("/dicono-di-noi/{id}")
    public String viewTestimonial(@PathVariable Long id, Model model) {
        Optional<Testimonial> testimonialOpt = testimonialService.getPublishedTestimonialById(id);
        
        if (testimonialOpt.isEmpty()) {
            return "redirect:/dicono-di-noi";
        }
        
        model.addAttribute("testimonial", testimonialOpt.get());
        model.addAttribute("pageTitle", "Testimonianza - Hicman Capital Partner");
        
        // Se vuoi una pagina dettaglio dedicata, crea testimonial-detail.html
        // Altrimenti redirect alla lista
        return "redirect:/dicono-di-noi";
    }

    @GetMapping("/blog/testimonials")
    public String blogTestimonialsRedirect() {
        return "redirect:/dicono-di-noi";
    }

    @GetMapping("/blog/{id:[0-9]+}")
    public String blogDetailRedirect(@PathVariable Long id) {
        return "redirect:/rassegna-stampa/" + id;
    }
}