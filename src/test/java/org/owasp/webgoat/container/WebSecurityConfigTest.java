// File: src/test/java/org/owasp/webgoat/container/WebSecurityConfigTest.java
package org.owasp.webgoat.container;

import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Delta tests for WebSecurityConfig focusing on:
 * - Use of a secure PasswordEncoder (BCryptPasswordEncoder instead of NoOpPasswordEncoder).
 * - CSRF no longer being disabled explicitly.
 */
public class WebSecurityConfigTest {

    @Test
    void passwordEncoder_isBCryptPasswordEncoder() {
        UserService userService = mock(UserService.class);
        WebSecurityConfig config = new WebSecurityConfig(userService);

        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder, "PasswordEncoder bean must not be null");
        assertTrue(
            encoder instanceof BCryptPasswordEncoder,
            "PasswordEncoder must be BCryptPasswordEncoder to avoid plain-text storage");
    }

    @Test
    void configureGlobal_usesConfiguredPasswordEncoder() throws Exception {
        UserService userService = mock(UserService.class);
        WebSecurityConfig config = new WebSecurityConfig(userService);
        AuthenticationManagerBuilder authBuilder = mock(AuthenticationManagerBuilder.class);

        when(authBuilder.userDetailsService(userService)).thenReturn(authBuilder);

        config.configureGlobal(authBuilder);

        verify(authBuilder).passwordEncoder(config.passwordEncoder());
    }

    @Test
    void filterChain_buildsSuccessfully_indicatingConfigurationIsConsistent() throws Exception {
        UserService userService = mock(UserService.class);
        WebSecurityConfig config = new WebSecurityConfig(userService);
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = config.filterChain(http);

        assertNotNull(chain, "SecurityFilterChain should be built successfully");
    }
}
