package com.jobpilot.recruiter.service;

import tools.jackson.databind.JsonNode;
import com.jobpilot.recruiter.dto.RecruiterSearchRequest;
import com.jobpilot.recruiter.dto.RecruiterSearchResult;
import com.jobpilot.recruiter.integration.SerpApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterSearchService {

    private final SerpApiClient serpApiClient;

public List<RecruiterSearchResult> searchRecruiters(
        RecruiterSearchRequest request) {

    String query = "site:linkedin.com/in/ \""
            + request.designation()
            + "\" \""
            + request.companyName()
            + "\"";

    if (request.location() != null
            && !request.location().isBlank()) {
        query += " \"" + request.location() + "\"";
    }

    JsonNode response = serpApiClient.search(query);

    JsonNode organicResults = response.path("organic_results");

    if (!organicResults.isArray()) {
        return List.of();
    }

    List<RecruiterSearchResult> results = new ArrayList<>();

    for (JsonNode item : organicResults) {

        String title = item.path("title").asText("");
        String link = item.path("link").asText("");
        String snippet = item.path("snippet").asText("");

        if (title.isBlank() || link.isBlank()) {
            continue;
        }

        try {
            URI uri = URI.create(link);

            String host = uri.getHost();
            String path = uri.getPath();

            if (host == null || path == null) {
                continue;
            }

            boolean validLinkedInProfile =
                    (host.equalsIgnoreCase("linkedin.com")
                    || host.equalsIgnoreCase("www.linkedin.com"))
                    && path.startsWith("/in/");

            if (!validLinkedInProfile) {
                continue;
            }

            results.add(new RecruiterSearchResult(
                    title,
                    request.designation(),
                    request.companyName(),
                    link,
                    snippet,
                    "LINKEDIN"
            ));

        } catch (IllegalArgumentException exception) {
            // Ignore malformed URLs.
        }
    }

    return results;
}

    private String buildSearchQuery(RecruiterSearchRequest request) {

        StringBuilder query = new StringBuilder();

        query.append("site:linkedin.com/in/ ")
                .append("\"").append(request.designation()).append("\" ")
                .append("\"").append(request.companyName()).append("\"");

        if (request.location() != null && !request.location().isBlank()) {
            query.append(" ").append(request.location());
        }

        return query.toString();
    }

    private String getText(JsonNode node, String fieldName) {

        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull()) {
            return null;
        }

        String text = value.asText();

        return text.isBlank() ? null : text;
    }
}