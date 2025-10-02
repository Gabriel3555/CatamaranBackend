package com.catamaran.catamaranbackend.auth.application.port;

import com.catamaran.catamaranbackend.auth.application.dto.RefreshTokenRequest;
import com.catamaran.catamaranbackend.auth.application.dto.RefreshTokenResponse;

public interface RefreshTokenUseCase {
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
}
