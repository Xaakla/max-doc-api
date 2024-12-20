package com.app.maxdocapi.models.dtos;

import com.app.maxdocapi.database.entities.Document;
import com.app.maxdocapi.enums.Phase;

import java.time.LocalDateTime;

public class DocumentListDto {
    private Long id;
    private String title;
    private String description;
    private String acronym;
    private int version;
    private Phase phase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DocumentListDto() {
    }

    public DocumentListDto(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.description = document.getDescription();
        this.acronym = document.getAcronym();
        this.version = document.getVersion();
        this.phase = document.getPhase();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
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

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
