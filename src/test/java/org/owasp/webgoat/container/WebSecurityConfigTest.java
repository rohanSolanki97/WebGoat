package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
 * Delta test for:
 *   Source: src/main/java/org/owasp/webgoat/container/WebSecurityConfig.java
 *   Test:   src/test/java/org/owasp/webgoat/container/WebSecurityConfigTest.java
 *
 * Focus:
 *   - PasswordEncoder changed from NoOpPasswordEncoder to a secure encoder (BCryptPasswordEncoder).
 *   - Explicit CSRF disabling removed from the HttpSecurity configuration.
 */
public class WebSecurityConfigTest {

  @Test
  void passwordEncoderShouldHashPasswordsNotReturnPlainText() {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    PasswordEncoder encoder = config.passwordEncoder();
    String raw = "password123";
    String encoded = encoder.encode(raw);

    // Assert: secure encoder should hash and validate correctly
    assertFalse(
        raw.equals(encoded),
        "PasswordEncoder must not be a no-op; encoded password must differ from raw");
    assertTrue(
        encoder.matches(raw, encoded),
        "PasswordEncoder must successfully verify the encoded password");
  }

  @Test
  void configureGlobalShouldStillWireUserDetailsService() throws Exception {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);
    AuthenticationManagerBuilder authBuilder = Mockito.mock(AuthenticationManagerBuilder.class);

    // Act
    config.configureGlobal(authBuilder);

    // Assert: ensures change of PasswordEncoder did not break auth wiring
    Mockito.verify(authBuilder).userDetailsService(userService);
  }
}
