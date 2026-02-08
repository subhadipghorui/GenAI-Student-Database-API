package com.example.genaistudentscore.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenAiQueryRequest {

    @NotBlank
    private String prompt;
}