package com.jobpilot.resume.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ResumeStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {

        validatePdfFile(file);

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        Files.createDirectories(uploadPath);

        String fileName = UUID.randomUUID() + ".pdf";

        Path targetPath = uploadPath.resolve(fileName).normalize();

        if (!targetPath.startsWith(uploadPath)) {
            throw new IOException("Invalid file path");
        }

        Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return targetPath.toString();
    }

    private void validatePdfFile(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a file");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 5MB");
        }

        byte[] fileBytes = file.getBytes();

        String fileHeader = new String(
                fileBytes,
                0,
                Math.min(fileBytes.length, 5),
                java.nio.charset.StandardCharsets.US_ASCII
        );

        if (!fileHeader.equals("%PDF-")) {
            throw new IllegalArgumentException("Only valid PDF files are allowed");
        }
    }

    public void deleteFile(String filePath) throws IOException {

        Path uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Path targetPath = Paths.get(filePath)
                .toAbsolutePath()
                .normalize();

        if (!targetPath.startsWith(uploadPath)) {
            throw new IOException("Invalid file path");
        }

        Files.deleteIfExists(targetPath);
    }


public org.springframework.core.io.Resource loadFile(String filePath)
        throws IOException {

    Path uploadPath = Paths.get(uploadDir)
            .toAbsolutePath()
            .normalize();

    Path targetPath = Paths.get(filePath)
            .toAbsolutePath()
            .normalize();

    if (!targetPath.startsWith(uploadPath)) {
        throw new IOException("Invalid file path");
    }

    if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
    throw new java.nio.file.NoSuchFileException(
            targetPath.toString()
    );
}

    return new org.springframework.core.io.UrlResource(
            targetPath.toUri()
    );
}

}
