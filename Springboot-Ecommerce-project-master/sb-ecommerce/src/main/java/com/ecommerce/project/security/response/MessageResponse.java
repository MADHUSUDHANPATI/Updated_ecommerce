package com.ecommerce.project.security.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MessageResponse {

    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
