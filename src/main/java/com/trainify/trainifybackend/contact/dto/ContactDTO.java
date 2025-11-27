package com.trainify.trainifybackend.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactDTO(
        @NotBlank String name,
        @NotBlank String message,
        @NotBlank String subject,
        @Email String email
) {
}
