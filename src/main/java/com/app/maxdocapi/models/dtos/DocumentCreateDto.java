package com.app.maxdocapi.models.dtos;

import jakarta.validation.constraints.NotBlank;

public class DocumentCreateDto {
    private Long id;
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Description is required")
    private String description;
    @NotBlank(message = "Acronym is required")
    private String acronym;
    @NotBlank(message = "Version is required")
    private int version;

    public DocumentCreateDto() {
    }

    public DocumentCreateDto(Long id, String title, String description, String acronym, int version) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.acronym = acronym;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
