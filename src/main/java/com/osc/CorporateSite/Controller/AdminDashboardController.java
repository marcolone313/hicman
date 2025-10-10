package com.osc.CorporateSite.Controller;

import com.osc.CorporateSite.Service.BlogService;
import com.osc.CorporateSite.Service.TestimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller per la Dashboard Admin
 * Gestisce la pagina principale dell'area amministrativa
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private TestimonialService testimonialService;

    /**
     * Dashboard principale admin
     * Mostra statistiche e ultimi contenuti creati
     * 
     * @param model Model per Thymeleaf
     * @return Template admin/dashboard.html
     */
    @GetMapping
    public String dashboard(Model model) {
        // ===== STATISTICHE BLOG =====
        long totalPosts = blogService.countAllPosts();
        long publishedPosts = blogService.countPublishedPosts();
        long draftPosts = blogService.countDraftPosts();
        long recentPostsCount = blogService.countRecentPosts(30); // ultimi 30 giorni
        
        // ===== STATISTICHE TESTIMONIAL =====
        long totalTestimonials = testimonialService.countAllTestimonials();
        long publishedTestimonials = testimonialService.countPublishedTestimonials();
        
        // ===== ULTIMI CONTENUTI =====
        // Ultimi 5 post (tutti, anche bozze)
        model.addAttribute("latestPosts", blogService.getLatestPosts(5));
        
        // Ultime 5 testimonianze (tutte, anche bozze)
        model.addAttribute("latestTestimonials", testimonialService.getLatestTestimonials(5));
        
        // ===== STATISTICHE PER LE CARD =====
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("publishedPosts", publishedPosts);
        model.addAttribute("draftPosts", draftPosts);
        model.addAttribute("recentPostsCount", recentPostsCount);
        model.addAttribute("totalTestimonials", totalTestimonials);
        model.addAttribute("publishedTestimonials", publishedTestimonials);
        
        // ===== META =====
        model.addAttribute("pageTitle", "Dashboard");
        
        return "admin/dashboard";
    }

    /**
     * Pagina di login admin
     * 
     * @return Template admin/login.html
     */
    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }
}