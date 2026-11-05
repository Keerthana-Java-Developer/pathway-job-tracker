package com.pathway.config;

import com.pathway.security.JwtAuthFilter;
import com.pathway.security.JwtService;
import com.pathway.repo.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"message\":\"Authentication required or session expired. Please log in again.\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"message\":\"You do not have permission to perform this action.\"}"
                            );
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/jobs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /*
     * JwtAuthFilter must run only inside Spring Security's filter chain.
     * Spring Boot otherwise auto-registers this @Component filter as a servlet filter too.
     */
    @Bean
    JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserRepository users) {
        return new JwtAuthFilter(jwtService, users);
    }

    @Bean
    FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();

        cors.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://pathway-job-tracker-2aep-9z54jvejn.vercel.app",
                "https://*.vercel.app"
        ));

        cors.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH",
                "DELETE", "OPTIONS", "HEAD"
        ));

        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(false);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
