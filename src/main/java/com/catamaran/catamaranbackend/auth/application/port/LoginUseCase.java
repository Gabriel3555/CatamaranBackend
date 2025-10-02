package com.catamaran.catamaranbackend.auth.application.port;


import com.catamaran.catamaranbackend.auth.application.dto.AuthRequest;
import com.catamaran.catamaranbackend.auth.application.dto.AuthResponse;

public interface LoginUseCase {
    AuthResponse login(AuthRequest authRequest);
}
