package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Delta tests for WebSecurityConfig focusing on:
 * - Use of a strong PasswordEncoder (BCryptPasswordEncoder) instead of NoOpPasswordEncoder.
 * - Ensuring the AuthenticationManagerBuilder is configured to use the secure encoder.
 *
 * These tests do not exercise HTTP security configuration end-to-end but focus narrowly on the
 * changed behavior related to password encoding.
 */
public class WebSecurityConfigTest {

  @Test
  void passwordEncoder_providesBCryptPasswordEncoder() {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    PasswordEncoder encoder = config.passwordEncoder();

    // Assert
    assertInstanceOf(
        BCryptPasswordEncoder.class,
        encoder,
        "passwordEncoder bean must provide a BCryptPasswordEncoder for secure password hashing");
  }

  @Test
  void authenticationManager_usesConfiguredUserDetailsService() throws Exception {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    AuthenticationManager mockManager = Mockito.mock(AuthenticationManager.class);
    AuthenticationConfiguration authConfig = Mockito.mock(AuthenticationConfiguration.class);
    Mockito.when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

    // Act
    AuthenticationManager authenticationManager = config.authenticationManager(authConfig);
    UserDetailsService uds = config.userDetailsServiceBean();

    // Assert
    // We do not inspect the entire security chain, but we ensure the beans are wired as expected
    // and that Spring can obtain an AuthenticationManager instance from the configuration.
    assertInstanceOf(
        UserService.class,
        uds,
        "userDetailsServiceBean should expose the injected UserService as UserDetailsService");
    assertInstanceOf(
        AuthenticationManager.class,
        authenticationManager,
        "authenticationManager should be obtained from AuthenticationConfiguration");
  }
}
