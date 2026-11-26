
package com.jobpilot.recruiter.controller;

import com.jobpilot.recruiter.dto.RecruiterSearchRequest;
import com.jobpilot.recruiter.dto.RecruiterSearchResult;
import com.jobpilot.recruiter.service.RecruiterSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiters/search")
@RequiredArgsConstructor
public class RecruiterSearchController {

    private final RecruiterSearchService recruiterSearchService;

    @PostMapping
    public ResponseEntity<List<RecruiterSearchResult>> searchRecruiters(
            @Valid @RequestBody RecruiterSearchRequest request) {

        List<RecruiterSearchResult> results =
                recruiterSearchService.searchRecruiters(request);

        return ResponseEntity.ok(results);
    }
}
