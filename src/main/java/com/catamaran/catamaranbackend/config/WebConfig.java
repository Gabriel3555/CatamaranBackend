package com.catamaran.catamaranbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for proper UTF-8 encoding support
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    static {
        // Set default encoding for HTTP responses
        System.setProperty("spring.http.encoding.charset", "UTF-8");
        System.setProperty("spring.http.encoding.enabled", "true");
        System.setProperty("spring.http.encoding.force", "true");
        System.setProperty("server.servlet.encoding.charset", "UTF-8");
        System.setProperty("server.servlet.encoding.enabled", "true");
        System.setProperty("server.servlet.encoding.force", "true");
    }
}