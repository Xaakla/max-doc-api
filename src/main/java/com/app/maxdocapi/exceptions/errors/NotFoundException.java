package com.app.maxdocapi.exceptions.errors;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String errorMassage) {
        super(errorMassage);
    }
}
