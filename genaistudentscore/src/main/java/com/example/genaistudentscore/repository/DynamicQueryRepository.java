package com.example.genaistudentscore.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DynamicQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public DynamicQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> execute(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
