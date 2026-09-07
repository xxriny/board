package com.xxrin.board.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xxrin.board.controller.AuthController;
import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.SignupRequest;
import com.xxrin.board.dto.response.MemberResponse;
import com.xxrin.board.exception.BusinessException;
import com.xxrin.board.exception.ErrorCode;
import com.xxrin.board.exception.GlobalExceptionHandler;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.repository.RefreshTokenRepository;
import com.xxrin.board.security.TokenService;
import com.xxrin.board.service.AuthService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class SignupTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void rejectsPasswordWithoutSpecialCharacter() {
        SignupRequest request = new SignupRequest(
                "user@example.com",
                "password123",
                "사용자",
                "01012345678");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("password");
    }

    @Test
    void signsUpWithNormalizedMemberAndEncodedPassword() {
        MemberRepository members = mock(MemberRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("password123!"))
                .thenReturn("encoded-password");
        when(members.saveAndFlush(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = new AuthService(
                members,
                mock(RefreshTokenRepository.class),
                passwordEncoder,
                mock(TokenService.class))
                .signup(new SignupRequest(
                        " USER@Example.com ",
                        "password123!",
                        "사용자",
                        "010-1234-5678"));

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.phone()).isEqualTo("01012345678");
        verify(passwordEncoder).encode("password123!");
    }

    @Test
    void rejectsDuplicateEmail() {
        MemberRepository members = mock(MemberRepository.class);
        when(members.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> new AuthService(
                members,
                mock(RefreshTokenRepository.class),
                mock(PasswordEncoder.class),
                mock(TokenService.class))
                .signup(new SignupRequest(
                        "USER@example.com",
                        "password123!",
                        "사용자",
                        "01012345678")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL));
    }

    @Test
    void signupEndpointReturnsCreatedMember() throws Exception {
        AuthService authService = mock(AuthService.class);
        when(authService.signup(any(SignupRequest.class)))
                .thenReturn(new MemberResponse(
                        1L,
                        "user@example.com",
                        "사용자",
                        "01012345678",
                        com.xxrin.board.domain.Role.USER,
                        null,
                        null));
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@example.com",
                                  "password":"password123!",
                                  "nickname":"사용자",
                                  "phone":"01012345678"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }
}
