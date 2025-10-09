-- Dati di esempio per BlogPost
INSERT INTO blog_posts (title, content, source_name, published_date, is_published, created_at, updated_at)
VALUES 
('Primo articolo di prova', 'Questo è il contenuto del primo articolo di prova per testare il sistema di blog.', 'Il Sole 24 Ore', CURRENT_TIMESTAMP, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Secondo articolo in bozza', 'Questo articolo è ancora in bozza e non è pubblicato.', 'Repubblica', NULL, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Articolo sulla finanza', 'Un articolo interessante sulla finanza strutturata e i mercati finanziari.', 'Milano Finanza', CURRENT_TIMESTAMP, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Dati di esempio per Testimonial
INSERT INTO testimonials (author, author_role, company_name, content, is_published, display_order, created_at)
VALUES 
('Mario Rossi', 'CEO', 'Azienda Alpha', 'Ottimo servizio, professionali e competenti. Consigliatissimi!', true, 1, CURRENT_TIMESTAMP),
('Laura Bianchi', 'CFO', 'Beta Corporation', 'Hanno gestito perfettamente la nostra operazione di finanza strutturata.', true, 2, CURRENT_TIMESTAMP),
('Giovanni Verdi', 'Managing Director', 'Gamma Group', 'Partner affidabile per le nostre operazioni finanziarie complesse.', false, 3, CURRENT_TIMESTAMP);