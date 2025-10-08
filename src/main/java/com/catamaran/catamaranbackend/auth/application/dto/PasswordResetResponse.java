package com.catamaran.catamaranbackend.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {
    private String message;
    private boolean success;
}