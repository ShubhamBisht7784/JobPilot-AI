
package com.jobpilot.resume.controller;

import com.jobpilot.resume.dto.ResumeResponse;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.resume.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.core.io.Resource;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        Resume resume = resumeService.uploadResume(file, authentication);

        ResumeResponse response = new ResumeResponse(
                resume.getId(),
                resume.getFileName(),
                resume.getFileType(),
                resume.getUploadedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


@GetMapping
public ResponseEntity<List<ResumeResponse>> getUserResumes(
        Authentication authentication) {

    List<ResumeResponse> responses = resumeService
            .getUserResumes(authentication)
            .stream()
            .map(resume -> new ResumeResponse(
                    resume.getId(),
                    resume.getFileName(),
                    resume.getFileType(),
                    resume.getUploadedAt()
            ))
            .toList();

    return ResponseEntity.ok(responses);
}


@GetMapping("/{id}")
public ResponseEntity<ResumeResponse> getResumeById(
        @PathVariable Long id,
        Authentication authentication) {

    Resume resume = resumeService.getResumeById(id, authentication);

    ResumeResponse response = new ResumeResponse(
            resume.getId(),
            resume.getFileName(),
            resume.getFileType(),
            resume.getUploadedAt()
    );

    return ResponseEntity.ok(response);
}

@DeleteMapping("/{id}")
public ResponseEntity<String> deleteResume(
        @PathVariable Long id,
        Authentication authentication) throws IOException {

    resumeService.deleteResume(id, authentication);

    return ResponseEntity.ok("Resume deleted successfully");
}


@GetMapping("/{id}/download")
public ResponseEntity<Resource> downloadResume(
        @PathVariable Long id,
        Authentication authentication) throws IOException {

    Resource resource = resumeService.downloadResume(id, authentication);

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + resource.getFilename() + "\""
            )
            .body(resource);
}

}
