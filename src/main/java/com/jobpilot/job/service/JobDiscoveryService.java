package com.jobpilot.job.service;

import com.jobpilot.job.dto.JobDiscoveryResponse;
import com.jobpilot.job.entity.Job;
import com.jobpilot.job.entity.JobSource;
import com.jobpilot.job.entity.JobStatus;
import com.jobpilot.job.repository.JobRepository;
import com.jobpilot.recruiter.integration.SerpApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class JobDiscoveryService {
    private final SerpApiClient serpApiClient;
    private final JobRepository jobRepository;

    public JobDiscoveryService(SerpApiClient serpApiClient, JobRepository jobRepository) {
        this.serpApiClient = serpApiClient; this.jobRepository = jobRepository;
    }

    @Transactional
    public JobDiscoveryResponse discover(String query) {
        JsonNode response = serpApiClient.searchGoogleJobs(query);
        JsonNode jobs = response.path("jobs_results");
        if (!jobs.isArray()) return new JobDiscoveryResponse(query, 0, 0, 0);

        int found = 0, saved = 0, duplicates = 0;
        for (JsonNode item : jobs) {
            String title = text(item, "title");
            String company = text(item, "company_name");
            String location = text(item, "location");
            String description = text(item, "description");
            String url = resolveJobUrl(item);
            if (title == null || company == null || url == null) continue;
            found++;

            String fingerprint = fingerprint(title, company, location, url);
            if (jobRepository.existsByJobUrl(url)
                    || jobRepository.existsByDeduplicationKey(fingerprint)) {
                duplicates++;
                continue;
            }

            Job job = new Job();
            job.setTitle(limit(title, 250));
            job.setCompanyName(limit(company, 200));
            job.setLocation(limit(location, 250));
            job.setDescription(limit(description, 30000));
            job.setJobUrl(limit(url, 1000));
            String provider = firstNonBlank(optionTitle(item), text(item, "via"));
            job.setSourceName(limit(provider, 160));
            job.setSource(mapSource(provider));
            job.setSalary(limit(readExtension(item, "salary"), 300));
            job.setEmploymentType(limit(readExtension(item, "schedule_type"), 120));
            job.setWorkMode(limit(readExtension(item, "work_from_home"), 80));
            job.setDeduplicationKey(fingerprint);
            job.setPostedAt(parsePostedAt(item));
            job.setStatus(JobStatus.OPEN);
            jobRepository.save(job);
            saved++;
        }
        return new JobDiscoveryResponse(query, found, saved, duplicates);
    }

    private String resolveJobUrl(JsonNode item) {
        JsonNode options = item.path("apply_options");
        JsonNode preferred = preferredApplyOption(options);
        String link = preferred == null ? null : text(preferred, "link");
        if (isAllowedHttpUrl(link)) return link;
        String shareLink = text(item, "share_link");
        return isAllowedHttpUrl(shareLink) ? shareLink : null;
    }

    private String optionTitle(JsonNode item) {
        JsonNode preferred = preferredApplyOption(item.path("apply_options"));
        return preferred == null ? null : text(preferred, "title");
    }

    private JsonNode preferredApplyOption(JsonNode options) {
        if (!options.isArray() || options.size() == 0) return null;
        for (String preferred : new String[]{"linkedin", "naukri", "indeed"}) {
            for (JsonNode option : options) {
                String title = text(option, "title");
                String link = text(option, "link");
                if (title != null && title.toLowerCase(Locale.ROOT).contains(preferred)
                        && isAllowedHttpUrl(link)) return option;
            }
        }
        for (JsonNode option : options) {
            if (isAllowedHttpUrl(text(option, "link"))) return option;
        }
        return null;
    }

    private String readExtension(JsonNode item, String key) {
        JsonNode extensions = item.path("detected_extensions");
        if (extensions.isObject()) {
            JsonNode value = extensions.get(key);
            if (value != null && !value.isNull()) {
                if (key.equals("work_from_home") && value.asBoolean(false)) return "Remote";
                return value.asText();
            }
        }
        JsonNode values = item.path("extensions");
        if (values.isArray()) {
            for (JsonNode value : values) {
                String text = value.asText("").trim();
                if (key.equals("salary") && text.matches("(?i).*(₹|INR|USD|\\$|salary|\\d+\\s*(LPA|per year|/yr)).*")) return text;
                if (key.equals("schedule_type") && text.matches("(?i).*(full.?time|part.?time|contract|internship).*")) return text;
                if (key.equals("work_from_home") && text.matches("(?i).*(remote|work from home|hybrid).*")) return text;
            }
        }
        return null;
    }

    private Instant parsePostedAt(JsonNode item) {
        String posted = text(item.path("detected_extensions"), "posted_at");
        if (posted == null) return null;
        String normalized = posted.toLowerCase(Locale.ROOT);
        if (normalized.contains("today") || normalized.contains("hour") || normalized.contains("minute")) return Instant.now();
        if (normalized.contains("yesterday")) return LocalDate.now().minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return null;
    }

    private String fingerprint(String title, String company, String location, String url) {
        String canonicalUrl;
        try {
            URI uri = URI.create(url);
            canonicalUrl = (uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT))
                    + (uri.getPath() == null ? "" : uri.getPath().replaceAll("/+$", ""));
        } catch (IllegalArgumentException ex) {
            canonicalUrl = url.toLowerCase(Locale.ROOT).split("[?#]", 2)[0];
        }
        String raw = normalize(title) + "|" + normalize(company) + "|"
                + normalize(location) + "|" + normalize(canonicalUrl);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{Alnum}]", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private boolean isAllowedHttpUrl(String value) {
        if (value == null) return false;
        try {
            URI uri = URI.create(value);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException ex) { return false; }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) return null;
        String result = value.asText("").trim();
        return result.isBlank() ? null : result;
    }

    private JobSource mapSource(String provider) {
        if (provider == null) return JobSource.GOOGLE_JOBS;
        String value = provider.toLowerCase(Locale.ROOT);
        if (value.contains("linkedin")) return JobSource.LINKEDIN;
        if (value.contains("naukri")) return JobSource.NAUKRI;
        if (value.contains("indeed")) return JobSource.INDEED;
        if (value.contains("company") || value.contains("careers")) return JobSource.COMPANY_WEBSITE;
        return JobSource.GOOGLE_JOBS;
    }

    private String firstNonBlank(String first, String second) {
        return first != null ? first : second;
    }

    private String limit(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
