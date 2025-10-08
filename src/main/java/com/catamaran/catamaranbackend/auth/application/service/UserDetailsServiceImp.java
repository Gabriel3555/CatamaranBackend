package com.catamaran.catamaranbackend.auth.application.service;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.catamaran.catamaranbackend.auth.application.dto.*;
import com.catamaran.catamaranbackend.auth.application.port.AuthenticateUseCase;
import com.catamaran.catamaranbackend.auth.application.port.LoginUseCase;
import com.catamaran.catamaranbackend.auth.application.port.RefreshTokenUseCase;
import com.catamaran.catamaranbackend.auth.application.port.SearchUsernameUseCase;
import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.auth.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements LoginUseCase, AuthenticateUseCase, SearchUsernameUseCase, RefreshTokenUseCase, UserDetailsService {

    private final UserRepositoryJpa userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponse login(AuthRequest authLoginRequest) {
        String username = authLoginRequest.username();
        String password = authLoginRequest.password();

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found!"));

        if (!userEntity.getStatus()) {
            throw new BadCredentialsException("Invalid username or password!");
        }

        Long id = userEntity.getId();

        Authentication authentication = this.authenticate(new UserPrincipal(id, username), password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtUtils.createToken(authentication);
        String refreshToken = jwtUtils.createRefreshToken(authentication);

        return new AuthResponse(userEntity.getId(), username, userEntity.getRole(), "logged successfully!", jwtToken, refreshToken,true);
    }

    @Override
    public Authentication authenticate(UserPrincipal userPrincipal, String password) {
        UserDetails userDetails = this.searchUserDetails(userPrincipal.username());

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userDetails.getAuthorities());
    }

    @Override
    public UserDetails searchUserDetails(String username) {
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user not found"));

        GrantedAuthority role = new SimpleGrantedAuthority("ROLE_".concat(userEntity.getRole().name()));

        return new User(username, userEntity.getPassword(), Set.of(role));
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        try {
            DecodedJWT decodedJWT = jwtUtils.verifyToken(request.refreshToken());
            String username = jwtUtils.extractUsername(decodedJWT);

            UserEntity userEntity = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));

            if (!userEntity.getStatus()) {
                throw new BadCredentialsException("Invalid username or password!");
            }

            GrantedAuthority role = new SimpleGrantedAuthority("ROLE_".concat(userEntity.getRole().name()));

            UserPrincipal userPrincipal = new UserPrincipal(userEntity.getId(), username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, Set.of(role)
            );

            String newAccessToken = jwtUtils.createToken(authentication);

            return new RefreshTokenResponse(newAccessToken);

        } catch (Exception e) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return null;
    }

}
