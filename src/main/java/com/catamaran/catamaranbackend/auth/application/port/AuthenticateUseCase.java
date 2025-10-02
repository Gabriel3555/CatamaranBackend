package com.catamaran.catamaranbackend.auth.application.port;

import com.catamaran.catamaranbackend.auth.application.dto.UserPrincipal;
import org.springframework.security.core.Authentication;

public interface AuthenticateUseCase {
    Authentication authenticate(UserPrincipal userPrincipal, String password);
}
