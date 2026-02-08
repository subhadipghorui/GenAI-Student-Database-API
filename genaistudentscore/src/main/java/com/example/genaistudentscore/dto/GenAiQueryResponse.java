package com.example.genaistudentscore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class GenAiQueryResponse {

    private String generatedSql;
    private List<Map<String, Object>> data;
}
