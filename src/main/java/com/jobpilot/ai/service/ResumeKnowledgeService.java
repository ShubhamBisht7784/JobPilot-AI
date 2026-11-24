package com.jobpilot.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeKnowledgeService {

    private static final int DIMENSIONS = 384;
    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 150;
    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}+#.]{2,}");

    private final JdbcTemplate jdbcTemplate;

    public ResumeKnowledgeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int indexResume(Long userId, Long resumeId, String text) {
        ensureSchema();
        List<String> chunks = splitIntoChunks(text);

        jdbcTemplate.update(
                "DELETE FROM resume_knowledge_chunks WHERE user_id = ? AND resume_id = ?",
                userId, resumeId);

        jdbcTemplate.batchUpdate(
                """
                INSERT INTO resume_knowledge_chunks
                    (user_id, resume_id, chunk_index, content, embedding)
                VALUES (?, ?, ?, ?, ?::vector)
                """,
                new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement statement, int index)
                            throws SQLException {
                        String chunk = chunks.get(index);
                        statement.setLong(1, userId);
                        statement.setLong(2, resumeId);
                        statement.setInt(3, index);
                        statement.setString(4, chunk);
                        statement.setString(5, toVectorLiteral(vectorize(chunk)));
                    }

                    @Override
                    public int getBatchSize() {
                        return chunks.size();
                    }
                });

        return chunks.size();
    }

    @Transactional
    public List<KnowledgeChunk> search(Long userId, Long resumeId, String query, int topK) {
        ensureSchema();
        String vector = toVectorLiteral(vectorize(query));

        return jdbcTemplate.query(
                """
                SELECT content, (embedding <=> ?::vector) AS distance
                FROM resume_knowledge_chunks
                WHERE user_id = ? AND resume_id = ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, rowNum) -> new KnowledgeChunk(
                        rs.getString("content"),
                        rs.getDouble("distance")),
                vector, userId, resumeId, vector, Math.max(1, Math.min(topK, 10)));
    }

    @Transactional
    public void deleteResumeIndex(Long userId, Long resumeId) {
        ensureSchema();
        jdbcTemplate.update(
                "DELETE FROM resume_knowledge_chunks WHERE user_id = ? AND resume_id = ?",
                userId, resumeId);
    }

    private void ensureSchema() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS resume_knowledge_chunks (
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
                    resume_id BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                    chunk_index INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    embedding vector(384) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                    CONSTRAINT uk_resume_knowledge_chunk UNIQUE (user_id, resume_id, chunk_index)
                )
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_resume_knowledge_user_resume
                ON resume_knowledge_chunks(user_id, resume_id)
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_resume_knowledge_embedding
                ON resume_knowledge_chunks USING hnsw (embedding vector_cosine_ops)
                """);
    }

    private List<String> splitIntoChunks(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Resume has no extractable text to index.");
        }

        List<String> chunks = new ArrayList<>();
        String normalized = text.replace("\r\n", "\n").trim();
        int start = 0;

        while (start < normalized.length()) {
            int end = Math.min(start + CHUNK_SIZE, normalized.length());

            if (end < normalized.length()) {
                int boundary = normalized.lastIndexOf('\n', end);
                if (boundary <= start + (CHUNK_SIZE / 2)) {
                    boundary = normalized.lastIndexOf(' ', end);
                }
                if (boundary > start + (CHUNK_SIZE / 2)) {
                    end = boundary;
                }
            }

            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end >= normalized.length()) {
                break;
            }
            start = Math.max(start + 1, end - CHUNK_OVERLAP);
        }
        return chunks;
    }

    /*
     * Local feature-hashing vectors avoid requiring a second paid API.
     * These are lexical vectors, not transformer-generated semantic embeddings.
     */
    private float[] vectorize(String text) {
        float[] vector = new float[DIMENSIONS];
        Matcher matcher = TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        List<String> tokens = new ArrayList<>();

        while (matcher.find()) {
            tokens.add(matcher.group());
        }

        for (int i = 0; i < tokens.size(); i++) {
            addToken(vector, tokens.get(i), 1.0f);
            if (i > 0) {
                addToken(vector, tokens.get(i - 1) + "_" + tokens.get(i), 0.5f);
            }
        }

        double norm = 0.0;
        for (float value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);

        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
        return vector;
    }

    private void addToken(float[] vector, String token, float weight) {
        int hash = token.hashCode();
        int index = Math.floorMod(hash, DIMENSIONS);
        float sign = (hash & 0x40000000) == 0 ? 1.0f : -1.0f;
        vector[index] += sign * weight;
    }

    private String toVectorLiteral(float[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        return builder.append(']').toString();
    }

    public record KnowledgeChunk(String content, double distance) {
    }
}
