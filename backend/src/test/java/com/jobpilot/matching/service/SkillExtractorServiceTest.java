package com.jobpilot.matching.service;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class SkillExtractorServiceTest {

    private final SkillExtractorService service =
            new SkillExtractorService();

    @Test
    void shouldExtractBasicSkills() {

        String text = "We need Java, Spring Boot and MySQL developer";

        List<String> skills = service.extractSkills(text);

        assertThat(skills)
                .contains("Java", "Spring Boot", "MySQL");
    }

    @Test
    void shouldExtractProgrammingLanguagesWithSpecialCharacters() {

        String text = "Experience in C, C++ and C# required";

        List<String> skills = service.extractSkills(text);

        assertThat(skills)
                .contains("C", "C++", "C#");
    }

    @Test
    void shouldNormalizeSkillAliases() {

        String text = "Experience with Postgres, SpringBoot and ReactJS";

        List<String> skills = service.extractSkills(text);

        assertThat(skills)
                .contains("PostgreSQL", "Spring Boot", "React");
    }

    @Test
    void shouldNotMatchJavaInsideJavaScript() {

        String text = "We need a JavaScript developer";

        List<String> skills = service.extractSkills(text);

        assertThat(skills)
                .contains("JavaScript")
                .doesNotContain("Java");
    }

    @Test
    void shouldReturnEmptyListForBlankText() {

        List<String> skills = service.extractSkills(" ");

        assertThat(skills).isEmpty();
    }

    @Test
    void shouldNotReturnDuplicateSkills() {

        String text = "React ReactJS React.js";

        List<String> skills = service.extractSkills(text);

        assertThat(skills.stream()
                .filter(skill -> skill.equals("React"))
                .count()).isEqualTo(1);
    }

    @Test
void shouldNotExtractJavaScriptFromNextJsOrNodeJs() {

    String text = "Experience with Next.js and Node.js";

    List<String> skills = service.extractSkills(text);

    assertThat(skills)
            .contains("Next.js", "Node.js")
            .doesNotContain("JavaScript");
}

@Test
void shouldExtractMultipleSkillAliasesCorrectly() {

    String text = """
            Experience with SpringBoot, ReactJS,
            Postgres, RESTful API and React.js.
            """;

    List<String> skills = service.extractSkills(text);

    assertThat(skills).contains(
            "Spring Boot",
            "React",
            "PostgreSQL",
            "REST APIs"
    );

    assertThat(skills.stream()
            .filter(skill -> skill.equals("React"))
            .count()).isEqualTo(1);
}

@Test
void shouldExtractSkillsRegardlessOfLetterCase() {

    String text = "JAVA, spring boot, REACT and postgresql";

    List<String> skills = service.extractSkills(text);

    assertThat(skills)
            .contains("Java", "Spring Boot", "React", "PostgreSQL");
}
}
