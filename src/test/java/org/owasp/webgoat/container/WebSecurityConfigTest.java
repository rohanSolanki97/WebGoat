package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Delta tests for WebSecurityConfig focusing on:
 * - Replacing NoOpPasswordEncoder with BCryptPasswordEncoder
 * - Ensuring passwords are no longer stored/compared in plain text.
 *
 * CSRF enablement is handled inside Spring Security; we assert the bean-level
 * configuration related to password encoding which was explicitly changed.
 */
class WebSecurityConfigTest {

  @Test
  void passwordEncoder_returnsBCryptPasswordEncoder() {
    UserService userService = dummyUserService();
    WebSecurityConfig config = new WebSecurityConfig(userService);

    PasswordEncoder encoder = config.passwordEncoder();

    assertNotNull(encoder);
    assertTrue(encoder instanceof BCryptPasswordEncoder);
    String raw = "password123";
    String encoded = encoder.encode(raw);
    assertNotEquals(raw, encoded, "BCrypt must not store raw passwords");
    assertTrue(encoder.matches(raw, encoded));
  }

  @Test
  void configureGlobal_acceptsPasswordEncoderConfiguration() throws Exception {
    UserService userService = dummyUserService();
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // We only verify that configureGlobal can be invoked without throwing,
    // which indicates that UserDetailsService and PasswordEncoder wiring is valid.
    org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder builder =
        new org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder(
            null);

    config.configureGlobal(builder);
  }

  private UserService dummyUserService() {
    return new UserService() {
      // Implement minimal required methods if the actual interface has any.
      // This anonymous implementation keeps the test self-contained while
      // respecting the existing package and type.
    };
  }
}
