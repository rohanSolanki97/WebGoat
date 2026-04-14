package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Delta tests for WebSecurityConfig focusing on:
 * - PasswordEncoder now using BCryptPasswordEncoder instead of NoOpPasswordEncoder.
 *
 * We avoid exercising the full HttpSecurity configuration here to keep the test
 * independent of a running Spring context while still validating the core
 * security improvement.
 */
public class WebSecurityConfigTest {

  @Test
  void passwordEncoderIsBCrypt() {
    // Arrange
    WebSecurityConfig config = new WebSecurityConfig();

    // Act
    PasswordEncoder encoder = config.passwordEncoder();

    // Assert
    assertInstanceOf(
        BCryptPasswordEncoder.class,
        encoder,
        "PasswordEncoder should be BCryptPasswordEncoder for secure password hashing");
  }
}
