package com.app.maxdocapi.enums;

public enum Phase {
    MINUTA("Minuta"),
    VIGENTE("Vigente"),
    OBSOLETO("Obsoleto");

    private final String description;

    Phase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
