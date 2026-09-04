package com.xxrin.board.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxrin.board.dto.response.ApiResponse;
import com.xxrin.board.exception.ErrorCode;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** JWT 인증 경계와 공개 API를 구성한다. */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(@Value("${auth.jwt-secret}") String encodedSecret) {
        byte[] secret = Base64.getDecoder().decode(encodedSecret);
        if (secret.length < 32) {
            throw new IllegalArgumentException("AUTH_JWT_SECRET은 256비트 이상이어야 합니다.");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/boards/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, ErrorCode.INVALID_ACCESS_TOKEN))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, ErrorCode.FORBIDDEN_RESOURCE)))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, ErrorCode.INVALID_ACCESS_TOKEN)))
                .build();
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(errorCode));
    }
}
