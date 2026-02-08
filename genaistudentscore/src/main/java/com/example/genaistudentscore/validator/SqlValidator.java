package com.example.genaistudentscore.validator;

import org.springframework.stereotype.Component;

@Component
public class SqlValidator {

    public void validate(String sql) {
        String normalized = sql.trim().toLowerCase();

        if (!normalized.startsWith("select")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed");
        }

        if (normalized.contains("delete")
                || normalized.contains("update")
                || normalized.contains("insert")
                || normalized.contains("drop")
                || normalized.contains("alter")
                || normalized.contains(";")) {
            throw new IllegalArgumentException("Dangerous SQL detected");
        }

        if (!normalized.contains("limit")) {
            throw new IllegalArgumentException("LIMIT clause is required");
        }
    }
}
