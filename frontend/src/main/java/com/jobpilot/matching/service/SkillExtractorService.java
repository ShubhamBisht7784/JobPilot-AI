package com.jobpilot.matching.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class SkillExtractorService {

    private static final List<String> SKILLS = List.of(
            "Java",
            "Spring Boot",
            "Spring Security",
            "Hibernate",
            "JPA",
            "REST APIs",
            "Microservices",
            "SQL",
            "MySQL",
            "PostgreSQL",
            "MongoDB",
            "React",
            "JavaScript",
            "HTML",
            "CSS",
            "Python",
            "Docker",
            "Kubernetes",
            "AWS",
            "Git",
            "Maven",
            "Redis",
            "Kafka",
            "C",
            "C++",
            "C#",
            "Go",
            "TypeScript",
            "Angular",
            "Next.js",
            "Express.js",
            "Node.js",
            "Redis",
            "Jenkins",
            "JUnit",
            "Mockito"
    );

    private static final Map<String, String> SKILL_ALIASES = Map.of(
            "postgres", "PostgreSQL",
            "springboot", "Spring Boot",
            "react.js", "React",
            "reactjs", "React",
            "js", "JavaScript",
            "rest api", "REST APIs",
            "restful api", "REST APIs"
    );

    private static final Map<String, Pattern> SKILL_PATTERNS
            = createSkillPatterns();

    public List<String> extractSkills(String text) {

        Set<String> extractedSkills = new LinkedHashSet<>();

        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        for (Map.Entry<String, Pattern> entry : SKILL_PATTERNS.entrySet()) {

            String skillName = entry.getKey();
            Pattern pattern = entry.getValue();

            if (pattern.matcher(text).find()) {

                String canonicalSkill = SKILL_ALIASES.getOrDefault(
                        skillName,
                        skillName
                );

                extractedSkills.add(canonicalSkill);
            }
        }

        return new ArrayList<>(extractedSkills);
    }

    private static Map<String, Pattern> createSkillPatterns() {

        Map<String, Pattern> patterns = new LinkedHashMap<>();

        for (String skill : SKILLS) {
            patterns.put(
                    skill,
                    Pattern.compile(
                            "(?i)(?<![\\p{Alnum}.+#])"
        + Pattern.quote(skill)
        + "(?![\\p{Alnum}.+#])")
            );
        }

        for (String alias : SKILL_ALIASES.keySet()) {
            patterns.put(
                    alias,
                    Pattern.compile(
                            "(?i)(?<![\\p{Alnum}.+#])"
        + Pattern.quote(alias)
        + "(?![\\p{Alnum}.+#])"
                    )
            );
        }

        return Map.copyOf(patterns);
    }
}
