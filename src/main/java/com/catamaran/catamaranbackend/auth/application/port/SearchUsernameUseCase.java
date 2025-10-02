package com.catamaran.catamaranbackend.auth.application.port;

import org.springframework.security.core.userdetails.UserDetails;

public interface SearchUsernameUseCase {
    UserDetails searchUserDetails(String username);
}
