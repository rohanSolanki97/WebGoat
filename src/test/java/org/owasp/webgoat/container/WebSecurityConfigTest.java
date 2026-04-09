package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Delta tests for WebSecurityConfig focusing on:
 * - Use of BCryptPasswordEncoder instead of NoOpPasswordEncoder.
 *
 * NOTE: The validator reported a syntax issue in WebSecurityConfig; these tests assume that
 * the updated file compiles after that syntax is corrected.
 */
public class WebSecurityConfigTest {

  @Test
  void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    BCryptPasswordEncoder encoder = config.passwordEncoder();

    // Assert
    assertNotNull(encoder, "passwordEncoder bean must not be null");
    String hash = encoder.encode("password");
    assertTrue(
        encoder.matches("password", hash),
        "BCryptPasswordEncoder must correctly verify hashed password");
  }
}
