package com.example.genaistudentscore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlGenerationService {
    private final WebClient webClient;

    @Value("${grok.api.url}")
    private String grokApiUrl;

    @Value("${grok.api.key}")
    private String grokApiKey;

    public SqlGenerationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Example Grok API payload:
     * {
     * "prompt": "Generate SQL for selecting all students"
     * }
     *
     * @param prompt
     * @return
     */
    public String fetchResponseFromGrok(Map<String, Object> systemMsg, Map<String, Object> userMsg) {

        Map<String, Object> requestBody = Map.of(
                "input", List.of(systemMsg, userMsg),
                "model", "grok-4-1-fast-reasoning",
                "response_format", Map.of("type", "json_object"));

        try {
            String response = webClient.post()
                    .uri(grokApiUrl)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + grokApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return parseGrokOutputText(response);
        } catch (WebClientResponseException e) {
            System.err.println("Grok API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "Error: Grok API returned " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("Grok API unexpected error: " + e.getMessage());
            return "Error: Unexpected Grok API error - " + e.getMessage();
        }
    }

    /**
     * Generates SQL data from prompt using Grok API with additional context.
     * 
     * @param prompt User prompt/question
     * @return Generated SQL string
     */
    public String generateSql(String prompt) {
        String SCHEMA_INFO = """
            Database Schema:

            1. classes table: id, name, status
            2. students table: id, first_name, last_name, gender, dob, age, class_id
            3. subjects table: id, name, class_id, description
            4. test_marks table: id, name, class_id, student_id, subject_id, marks, total_marks

            Relationships:
            - Student belongs to one Class (students.class_id -> classes.id)
            - Subject belongs to one Class (subjects.class_id -> classes.id)
            - TestMarks belongs to Student, Class, and Subject

            """;

        String CUSTOM_INSTRUCTIONS = """
            Exceptions:
            - LIMIT clause is required for each SQL query to prevent large result sets. If the user does not specify a limit, default to "LIMIT 10000".
            - When querying subjects: Subject names in the database include the class name as a suffix (e.g., "Math Five", "Physics Six"). If the user asks about a subject without specifying the class (e.g., "Math"), use LIKE pattern matching to find all matching subjects (e.g., WHERE name LIKE 'Math%').
            - Always include related data in SELECT: When a query involves relationships (e.g., students and their marks), include relevant fields from all related tables. For example, if asking about "Student with highest marks in Math", return student columns (first_name, last_name, etc.), marks columns (marks, total_marks), and subject columns (subject name) in a single result set using JOINs.

            """;
        String additionalContext = SCHEMA_INFO + "\n" + CUSTOM_INSTRUCTIONS;
        String fullPrompt = prompt + "\n" + additionalContext;

        // Compose system and user messages
        Map<String, Object> systemMsg = Map.of(
                "role", "system",
                "content", "You are a SQL expert. Generate only valid PostgreSQL queries.");
        Map<String, Object> userMsg = Map.of(
                "role", "user",
                "content", fullPrompt);

        String grokResponse = fetchResponseFromGrok(systemMsg, userMsg);
        // System.out.println("grokResponse: " + grokResponse);

        // Extract the generated SQL query from the Grok response and return it
        return extractSqlFromGrokResponse(grokResponse);
    }

    /**
     * For simple chat responses that are not SQL queries, we can use the same
     * fetchResponseFromGrok method but with different system instructions to
     * generate a natural language answer instead of SQL.
     * This allows us to reuse the same Grok integration for both SQL generation and
     * general Q&A, while still providing the necessary context and instructions for
     * each use case.
     */

    public String generateAnswer(String prompt) {
        // Compose system and user messages
        Map<String, Object> systemMsg = Map.of(
                "role", "system",
                "content",
                "You are a helpful assistant that provides answers based on the database schema and relationships. Answer in natural language.");
        Map<String, Object> userMsg = Map.of(
                "role", "user",
                "content", prompt);

        return fetchResponseFromGrok(systemMsg, userMsg);
    }

    /**
     * Parse Grok API response to extract the generated SQL query or output text.
     * 
     * @param grokResponse JSON string from Grok API
     * @return Extracted text or null if not found
     */
    public static String parseGrokOutputText(String grokResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(grokResponse);
            JsonNode outputArray = root.path("output");
            if (outputArray.isArray() && outputArray.size() > 0) {
                JsonNode contentArray = outputArray.get(0).path("content");
                if (contentArray.isArray() && contentArray.size() > 0) {
                    JsonNode textNode = contentArray.get(0).path("text");
                    if (!textNode.isMissingNode()) {
                        return textNode.asText();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Grok response: " + e.getMessage());
        }
        return null;
    }

    /**
     * Parse SQL from Grok response
     */
    public String extractSqlFromGrokResponse(String grokResponse) {
        if (grokResponse == null)
            return null;
        // Try to extract SQL code block
        Pattern codeBlockPattern = Pattern.compile("(?s)```sql\\s*(.+?)\\s*```");
        Matcher codeBlockMatcher = codeBlockPattern.matcher(grokResponse);
        String sql = null;
        if (codeBlockMatcher.find()) {
            sql = codeBlockMatcher.group(1).trim();
        } else {
            // Fallback: extract first SQL-like statement (starts with
            // SELECT/INSERT/UPDATE/DELETE)
            Pattern sqlPattern = Pattern.compile("(?i)(SELECT|INSERT|UPDATE|DELETE)[\\s\\S]+?;");
            Matcher sqlMatcher = sqlPattern.matcher(grokResponse);
            if (sqlMatcher.find()) {
                sql = sqlMatcher.group(0).trim();
            } else {
                // If nothing found, return the whole string as fallback
                sql = grokResponse.trim();
            }
        }
        // Remove trailing semicolon if present
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        return sql;
    }

}