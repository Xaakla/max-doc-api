package com.app.maxdocapi.models.records;

import jakarta.validation.constraints.NotBlank;

public record DocumentEditInfoDto(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Description is required")
        String description) {
}
