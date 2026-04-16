package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Delta tests for WebSecurityConfig focusing on:
 * - Replacing NoOpPasswordEncoder with a secure PasswordEncoder (BCryptPasswordEncoder).
 * - Wiring of AuthenticationManager with the configured password encoder.
 *
 * These tests validate that:
 * - passwordEncoder() returns a bcrypt-based encoder (not NoOp).
 * - authenticationManager() bean can be created using the provided AuthenticationConfiguration.
 *
 * Note: Direct verification of CSRF being enabled at the HttpSecurity level would require the
 * Spring Test security infrastructure; here we focus on the password encoder delta which is a
 * core part of the fix and can be asserted deterministically.
 */
public class WebSecurityConfigTest {

  @Test
  void passwordEncoder_isBcryptBasedAndNotNoOp() {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    PasswordEncoder encoder = config.passwordEncoder();

    // Assert
    assertNotNull(encoder, "PasswordEncoder bean must not be null");
    String encoded = encoder.encode("password");
    assertTrue(
        encoded.startsWith("$2a$") || encoded.startsWith("$2b$") || encoded.startsWith("$2y$"),
        "PasswordEncoder should use bcrypt and produce a bcrypt hash");
  }

  @Test
  void authenticationManager_beanCanBeObtainedFromConfiguration() throws Exception {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);
    AuthenticationConfiguration authConfig = Mockito.mock(AuthenticationConfiguration.class);
    AuthenticationManager manager = Mockito.mock(AuthenticationManager.class);
    Mockito.when(authConfig.getAuthenticationManager()).thenReturn(manager);

    // Act
    AuthenticationManager resultingManager = config.authenticationManager(authConfig);

    // Assert
    assertNotNull(resultingManager, "AuthenticationManager bean must not be null");
  }
}
