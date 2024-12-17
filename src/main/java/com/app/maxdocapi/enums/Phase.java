package com.app.maxdocapi.enums;

public enum Phase {
    DRAFT("Draft"),
    ACTIVE("Active"),
    OBSOLETE("Obsolete");

    private final String description;

    Phase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
