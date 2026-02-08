package com.example.genaistudentscore.controller;

import com.example.genaistudentscore.dto.GenAiQueryRequest;
import com.example.genaistudentscore.dto.GenAiQueryResponse;
import com.example.genaistudentscore.repository.DynamicQueryRepository;
import com.example.genaistudentscore.validator.SqlValidator;
import com.example.genaistudentscore.service.SqlGenerationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/genai")
public class GenAiController {

    private final SqlGenerationService sqlService;
    private final SqlValidator sqlValidator;
    private final DynamicQueryRepository repository;

    public GenAiController(SqlGenerationService sqlService,
                           SqlValidator sqlValidator,
                           DynamicQueryRepository repository) {
        this.sqlService = sqlService;
        this.sqlValidator = sqlValidator;
        this.repository = repository;
    }

    @PostMapping("/query")
    public GenAiQueryResponse query(@Valid @RequestBody GenAiQueryRequest request) {
        System.out.println("Generating sql query for prompt: " + request.getPrompt());
        String sql = sqlService.generateSql(request.getPrompt());

        System.out.println("SQL: " + sql);

        sqlValidator.validate(sql);

        List<Map<String, Object>> result = repository.execute(sql);

        return new GenAiQueryResponse(sql, result);
    }

    @PostMapping("/chat")
    public GenAiQueryResponse chat(@Valid @RequestBody GenAiQueryRequest request) {
        System.out.println("Fetching response for prompt: " + request.getPrompt());
        String response = sqlService.generateAnswer(request.getPrompt());
        return new GenAiQueryResponse(response, null);
    }
}
