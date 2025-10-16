package com.catamaran.catamaranbackend.auth.application.dto;

import com.catamaran.catamaranbackend.domain.Role;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "username", "role", "message", "jwt", "status"})
public record AuthResponse(Long id, String username, Role role, String message, String jwt, boolean status) {}
