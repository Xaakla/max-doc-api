package com.app.maxdocapi.models.projections;

import com.app.maxdocapi.enums.Phase;

import java.time.LocalDateTime;

public interface DocumentListProjection {
    Long getId();
    String getTitle();
    String getDescription();
    String getAcronym();
    int getVersion();
    Phase getPhase();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
