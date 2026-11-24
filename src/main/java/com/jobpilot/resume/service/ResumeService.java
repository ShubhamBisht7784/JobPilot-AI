package com.jobpilot.resume.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.common.exception.ResumeNotFoundException;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.resume.repository.ResumeRepository;
import com.jobpilot.resume.util.PdfTextExtractor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import org.springframework.core.io.Resource;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeStorageService storageService;
    private final PdfTextExtractor pdfTextExtractor;

    public ResumeService(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            ResumeStorageService storageService,
            PdfTextExtractor pdfTextExtractor) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    @Transactional
    public Resume uploadResume(
            MultipartFile file,
            Authentication authentication) throws IOException {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        String filePath = storageService.storeFile(file);

        try {
            String extractedText
                    = pdfTextExtractor.extractText(file.getBytes());

            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalArgumentException(
                        "Unable to extract text from PDF");
            }

            Resume resume = new Resume();
            resume.setUser(user);
            resume.setFileName(safeOriginalFileName(file.getOriginalFilename()));
            resume.setFilePath(filePath);
            resume.setExtractedText(extractedText);
            resume.setFileType("PDF");

            return resumeRepository.save(resume);

        } catch (IOException | RuntimeException exception) {

            try {
                storageService.deleteFile(filePath);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }


    private String safeOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "resume.pdf";
        }

        String normalized = originalFileName.replace('\\', '/');
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (fileName.isBlank()) {
            return "resume.pdf";
        }
        return fileName.length() > 255 ? fileName.substring(0, 255) : fileName;
    }

    public List<Resume> getUserResumes(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        return resumeRepository.findByUser(user);
    }

    public Resume getResumeById(
            Long resumeId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        return resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(()
                        -> new ResumeNotFoundException("Resume not found"));
    }

    public void deleteResume(
            Long resumeId,
            Authentication authentication) throws IOException {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(()
                        -> new ResumeNotFoundException("Resume not found"));

        storageService.deleteFile(resume.getFilePath());

        resumeRepository.delete(resume);
    }

    public Resource downloadResume(
            Long resumeId,
            Authentication authentication) throws IOException {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(()
                        -> new ResumeNotFoundException("Resume not found"));

        try {
            return storageService.loadFile(resume.getFilePath());
        } catch (java.nio.file.NoSuchFileException exception) {
            throw new ResumeNotFoundException("Resume file not found");
        }
    }

}
