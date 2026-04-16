package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Delta unit tests for WebSecurityConfig focusing on:
 * - CSRF protection enabled with CookieCsrfTokenRepository
 * - BCryptPasswordEncoder usage for password hashing
 */
public class WebSecurityConfigTest {

    private WebSecurityConfig config;

    @Mock
    private HttpSecurity httpSecurity;
    @Mock
    private AuthenticationManagerBuilder authBuilder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        config = new WebSecurityConfig();
    }

    @Test
    public void testPasswordEncoderIsBCrypt() {
        assertTrue(config.passwordEncoder() instanceof BCryptPasswordEncoder,
                "PasswordEncoder should be BCryptPasswordEncoder");
    }

    @Test
    public void testConfigureAuthenticationUsesHashedPassword() throws Exception {
        AuthenticationManagerBuilder authBuilderMock = mock(AuthenticationManagerBuilder.class);
        AuthenticationManagerBuilder.InMemoryUserDetailsManagerConfigurer<?> inMemoryConfig =
                mock(AuthenticationManagerBuilder.InMemoryUserDetailsManagerConfigurer.class);
        when(authBuilderMock.inMemoryAuthentication()).thenReturn(inMemoryConfig);
        when(inMemoryConfig.withUser(anyString())).thenReturn(inMemoryConfig);
        when(inMemoryConfig.password(anyString())).thenReturn(inMemoryConfig);
        when(inMemoryConfig.roles(anyString())).thenReturn(inMemoryConfig);

        config.configure(authBuilderMock);

        verify(inMemoryConfig).password(argThat(pwd -> pwd.startsWith("$2") && pwd.length() > 20));
    }

    @Test
    public void testCsrfProtectionEnabled() throws Exception {
        HttpSecurity httpMock = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        config.configure(httpMock);
        verify(httpMock.csrf()).csrfTokenRepository(any(CookieCsrfTokenRepository.class));
    }
}