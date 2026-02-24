package com.hicman.CorporateSite.Controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hicman.CorporateSite.Service.BlogService;
import com.hicman.CorporateSite.Service.TestimonialService;

import jakarta.servlet.http.HttpServletResponse;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * Dashboard principale admin
     * Mostra statistiche e ultimi contenuti creati
     * 
     * @param model Model per Thymeleaf
     * @return Template admin/dashboard.html
     */
    @GetMapping
    public String dashboard(@RequestParam(required = false) String dbError, Model model) {
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

        // ===== DB DOWNLOAD ERROR =====
        if ("true".equals(dbError)) {
            model.addAttribute("dbErrorMessage", "Password errata. Download non autorizzato.");
        }

        return "admin/dashboard";
    }

    /**
     * Download del database H2 con verifica password aggiuntiva.
     * L'endpoint è già protetto da Spring Security (richiede ROLE_ADMIN).
     * La verifica della password è un secondo livello di protezione.
     */
    @PostMapping("/db-download")
    public void downloadDatabase(
            @RequestParam("dbPassword") String dbPassword,
            HttpServletResponse response) throws IOException {

        if (!dbPassword.equals(adminPassword)) {
            response.sendRedirect("/admin?dbError=true");
            return;
        }

        // Forza H2 a scrivere tutto su disco prima del download
        try {
            jdbcTemplate.execute("CHECKPOINT SYNC");
        } catch (Exception ignored) {
            // Continua anche se CHECKPOINT fallisce
        }

        File dbFile = new File("data/hicmandb.mv.db").getAbsoluteFile();
        if (!dbFile.exists()) {
            response.sendRedirect("/admin?dbError=true");
            return;
        }

        String filename = "hicmandb_backup_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ".mv.db";

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentLengthLong(dbFile.length());

        try (FileInputStream fis = new FileInputStream(dbFile)) {
            StreamUtils.copy(fis, response.getOutputStream());
        }
        response.flushBuffer();
    }

    /**
     * Ripristino del database H2 da file .mv.db caricato.
     * Strategia: esporta il DB caricato in SQL tramite H2 Script tool,
     * quindi esegue DROP ALL OBJECTS + RunScript sul DB corrente.
     * Richiede verifica password come secondo livello di protezione.
     */
    @PostMapping("/db-upload")
    public String uploadDatabase(
            @RequestParam("dbPassword") String dbPassword,
            @RequestParam("dbFile") MultipartFile dbFile,
            RedirectAttributes redirectAttributes) {

        // Verifica password
        if (!dbPassword.equals(adminPassword)) {
            redirectAttributes.addFlashAttribute("dbUploadError", "Password errata. Ripristino non autorizzato.");
            return "redirect:/admin";
        }

        // Verifica file
        if (dbFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("dbUploadError", "Nessun file selezionato.");
            return "redirect:/admin";
        }
        String originalFilename = dbFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".mv.db")) {
            redirectAttributes.addFlashAttribute("dbUploadError", "File non valido. Caricare un file .mv.db scaricato da questo sistema.");
            return "redirect:/admin";
        }

        // Percorsi temporanei
        Path tempMvDb  = Paths.get("data/hicmandb_restore_temp.mv.db").toAbsolutePath();
        Path tempDbUrl = Paths.get("data/hicmandb_restore_temp").toAbsolutePath();
        Path scriptFile = Paths.get("data/hicmandb_restore.sql").toAbsolutePath();

        try {
            // 1. Salva il file caricato come DB temporaneo separato
            Files.copy(dbFile.getInputStream(), tempMvDb, StandardCopyOption.REPLACE_EXISTING);

            // 2. Esporta il DB caricato in SQL tramite comando H2 nativo SCRIPT TO
            //    (connessione separata al file temporaneo, non interferisce con il DB principale)
            String scriptPathSql = scriptFile.toString().replace("\\", "/");
            String tempDbUrlStr  = "jdbc:h2:file:" + tempDbUrl.toString().replace("\\", "/") + ";IFEXISTS=TRUE";
            try (Connection exportConn = DriverManager.getConnection(tempDbUrlStr, "sa", "")) {
                exportConn.createStatement().execute("SCRIPT TO '" + scriptPathSql + "'");
            }

            // 3. Applica al DB corrente: DROP ALL + RUNSCRIPT FROM
            try (Connection conn = dataSource.getConnection()) {
                conn.createStatement().execute("DROP ALL OBJECTS");
                conn.createStatement().execute("RUNSCRIPT FROM '" + scriptPathSql + "'");
            }

            redirectAttributes.addFlashAttribute("dbUploadSuccess",
                "Database ripristinato con successo. Tutti i dati sono stati aggiornati.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("dbUploadError",
                "Errore durante il ripristino: " + e.getMessage());
        } finally {
            try { Files.deleteIfExists(tempMvDb); }   catch (IOException ignored) {}
            try { Files.deleteIfExists(scriptFile); } catch (IOException ignored) {}
        }

        return "redirect:/admin";
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