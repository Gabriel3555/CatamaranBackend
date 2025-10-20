package com.catamaran.catamaranbackend.auth.security;

import com.catamaran.catamaranbackend.auth.application.service.UserDetailsServiceImp;
import com.catamaran.catamaranbackend.auth.security.filter.JwtTokenValidator;
import com.catamaran.catamaranbackend.auth.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig  {

    private final JwtUtils jwtUtils;

    @Bean
    SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(http -> {
                     http.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                     http.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll();
                     http.requestMatchers(HttpMethod.POST, "/api/v1/auth/forgot-password").permitAll();
                     http.requestMatchers(HttpMethod.POST, "/api/v1/auth/reset-password").permitAll();
                     http.requestMatchers(HttpMethod.GET, "/api/v1/auth/validate-reset-token").permitAll();
                     http.requestMatchers(
                             "/login.html",
                             "/login.css",
                             "/login.js",
                             "/forgot-password.html",
                             "/reset-password.html",
                             "/logo-alianza.jpg",
                             "/common-pagination.css",
                             "/common-pagination.js",
                             "/logo-manta.jpg",
                             "/admin/**",
                             "/owner/**",
                             "/documents/**",
                             "/swagger-ui/**",
                             "/v3/api-docs/**",
                             "/v3/api-docs.yaml"
                     ).permitAll();
                    http.requestMatchers(HttpMethod.GET, "/api/v1/boat/documents/**").permitAll();
                    http.requestMatchers("/api/v1/payments/*/download-receipt").authenticated();
                     http.requestMatchers("/api/v1/boat/documents/**").authenticated();
                     http.requestMatchers("/api/v1/reports/**").hasAuthority("ROLE_ADMIN");
                     http.requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN");
                     http.requestMatchers(HttpMethod.GET, "/api/v1/auth/{id}").hasAnyAuthority("ROLE_PROPIETARIO", "ROLE_ADMIN");
                     http.requestMatchers(HttpMethod.PUT, "/api/v1/auth/{id}").hasAnyAuthority("ROLE_PROPIETARIO", "ROLE_ADMIN");
                     http.requestMatchers(HttpMethod.PATCH, "/api/v1/auth/{id}/password").hasAnyAuthority("ROLE_PROPIETARIO", "ROLE_ADMIN");
                     http.requestMatchers("/api/v1/auth/**").hasAuthority("ROLE_ADMIN");
                     http.requestMatchers("/api/v1/payments/**").hasAuthority("ROLE_ADMIN");
                     http.requestMatchers("/api/v1/maintenances/**").hasAuthority("ROLE_ADMIN");
                     http.requestMatchers("/api/v1/owner/**").hasAuthority("ROLE_PROPIETARIO");
                     http.requestMatchers("/api/v1/boat/**").hasAuthority("ROLE_ADMIN");
                     http.anyRequest().authenticated();
                 });

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsServiceImp userDetailsService){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}