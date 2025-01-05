package com.example.OngVeterinaria.Error;

public class ErrorResponse {
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    // Getters e Setters
}
