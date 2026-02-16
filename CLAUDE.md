# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Hicman Corporate Site — a bilingual (Italian/English) corporate website for a financial boutique firm. Built with Spring Boot 3.4.1, Java 17, Thymeleaf, and H2 embedded database.

## Build & Run Commands

```bash
# Run development server (port 8082)
mvn spring-boot:run

# Build JAR
mvn clean package

# Run tests
mvn test

# Docker build (requires JAR built first)
docker build -t hicmancorporatesitev1 .
docker run -d -p 8082:8082 hicmancorporatesitev1
```

Maven wrapper (`./mvnw`) is available if Maven is not installed globally.

## Architecture

Standard Spring Boot MVC with layered architecture under `src/main/java/com/hicman/CorporateSite/`:

- **Controller/** — Web request handlers. Public pages (`MainController`, `AboutController`, `ServicesController`, `PressController`) and admin CRUD (`AdminDashboardController`, `AdminBlogController`, `AdminTestimonialController`)
- **Model/** — JPA entities: `BlogPost` (press articles), `Testimonial` (media quotes), `Contact` (form DTO with honeypot spam protection)
- **Repository/** — Spring Data JPA interfaces (`BlogPostRepository`, `TestimonialRepository`)
- **Service/** — Business logic: `BlogService`, `TestimonialService`, `ContactService` (AWS Lambda email integration), `FileStorageService` (image uploads)
- **Config/** — `SecurityConfig` (Spring Security with in-memory auth), `WebConfig` (i18n, locale resolution), `FileUploadConfig`, `SmartLocaleResolver`

## Key Configuration

- **Server port:** 8082
- **Database:** H2 file-based at `./data/hicmandb` (auto-schema via `ddl-auto=update`)
- **Admin auth:** In-memory Spring Security (credentials in `application.properties`)
- **Email:** Contact form sends data to AWS Lambda endpoint
- **File uploads:** Stored in `uploads/` directory, max 10MB, allowed: jpg/jpeg/png/gif/webp
- **i18n:** Italian (default) and English via `messages_it.properties` / `messages_en.properties`; cookie-based locale persistence, switchable with `?lang=it` or `?lang=en`
- **Session timeout:** 30 minutes

## Routing

**Public:** `/`, `/about`, `/chi-siamo`, `/services`, `/servizi/**`, `/contact`, `/contatti`, `/rassegna-stampa/**`, `/dicono-di-noi/**`

**Admin (authenticated):** `/admin`, `/admin/login`, `/admin/blog/**`, `/admin/testimonials/**`

## Templates & Frontend

Thymeleaf templates in `src/main/resources/templates/`:
- Public pages at root level (`index.html`, `chi-siamo.html`, `servizi.html`, etc.)
- Admin templates in `admin/` subdirectory
- Shared fragments: `header.html`, `navbar.html`, `footer.html`

Static assets in `src/main/resources/static/`: CSS files organized by component/page, fonts, and images. No JavaScript build system — plain CSS and Thymeleaf templating.

## Image Dimensions (from design spec)

- **Business case cards:** 1000x1000px (25% top for title, 75% for image with overlay)
- **Business unit cards:** 800x1200px (20% top for title, 80% for image with overlay)

## Working Language

The codebase, comments, and commit messages are predominantly in Italian. The user communicates in Italian.
