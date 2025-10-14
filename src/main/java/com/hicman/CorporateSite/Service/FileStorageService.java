package com.hicman.CorporateSite.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Salva un file e restituisce il percorso relativo
     */
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        // Normalizza il nome del file
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Verifica che il file non sia vuoto
        if (file.isEmpty()) {
            throw new IOException("Impossibile salvare un file vuoto: " + originalFileName);
        }
        
        // Verifica che il nome del file sia valido
        if (originalFileName.contains("..")) {
            throw new IOException("Nome file non valido: " + originalFileName);
        }
        
        // Genera un nome univoco per il file
        String fileExtension = "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFileName.substring(lastDotIndex);
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Crea la directory se non esiste
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Salva il file
        Path targetLocation = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Restituisce il percorso relativo
        return "/" + uploadDir + "/" + subDirectory + "/" + uniqueFileName;
    }

    /**
     * Elimina un file dato il percorso
     */
    public void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                // Rimuovi il primo '/' se presente
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                
                Path path = Paths.get(filePath);
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'eliminazione del file: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Verifica se un file esiste
     */
    public boolean fileExists(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                Path path = Paths.get(filePath);
                return Files.exists(path);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ottiene la dimensione del file in bytes
     */
    public long getFileSize(String filePath) throws IOException {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        Path path = Paths.get(filePath);
        return Files.size(path);
    }

    /**
     * Verifica il tipo MIME del file
     */
    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Verifica la dimensione massima del file (in MB)
     */
    public boolean isFileSizeValid(MultipartFile file, long maxSizeMB) {
        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        return file.getSize() <= maxSizeBytes;
    }

    /**
     * Ottiene l'estensione del file
     */
    public String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Verifica se l'estensione è permessa
     */
    public boolean isExtensionAllowed(String fileName, String[] allowedExtensions) {
        String extension = getFileExtension(fileName);
        for (String allowed : allowedExtensions) {
            if (extension.equals(allowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}