package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Delta tests for WebSecurityConfig focusing on the new strong PasswordEncoder.
 *
 * These tests verify only the changed behavior around passwordEncoder(), which is
 * straightforward to assert in isolation.
 */
class WebSecurityConfigTest {

  @Test
  void passwordEncoder_returnsBCryptPasswordEncoder() {
    UserService userService = null; // not used by passwordEncoder()
    WebSecurityConfig config = new WebSecurityConfig(userService);

    PasswordEncoder encoder = config.passwordEncoder();

    assertInstanceOf(BCryptPasswordEncoder.class, encoder);
  }
}
