package com.vehiclerental.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            Path testFile = this.fileStorageLocation.resolve(".write_test");
            Files.write(testFile, "test".getBytes());
            Files.delete(testFile);
        } catch (Exception ex) {
            throw new IllegalStateException("Upload directory " + this.fileStorageLocation + " is not writable or could not be created. Please check volume permissions (e.g., RAILWAY_RUN_UID).", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalName);
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new RuntimeException("Only JPG and PNG files are allowed");
        }
        
        // Validate magic bytes
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            if (is.read(header) != -1) {
                boolean isJpeg = header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
                boolean isPng = header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47;
                if (!isJpeg && !isPng) {
                    throw new RuntimeException("Invalid file signature. Only JPG/PNG are allowed.");
                }
            } else {
                throw new RuntimeException("File is empty or corrupted.");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read file signature.", ex);
        }

        String extension = originalName.substring(originalName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            String fileName = fileUrl.substring("/uploads/".length());
            try {
                Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
                Files.deleteIfExists(filePath);
            } catch (IOException ex) {
                System.err.println("Could not delete file: " + fileName);
            }
        }
    }
}
