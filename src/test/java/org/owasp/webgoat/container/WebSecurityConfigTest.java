// batch_id: BATCH-005
// status: IN_PROGRESS
// test_file_path: src/test/java/org/owasp/webgoat/container/WebSecurityConfigTest.java
package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Delta tests for WebSecurityConfig focused on:
 * - Replacement of NoOpPasswordEncoder with BCryptPasswordEncoder.
 */
public class WebSecurityConfigTest {

  @Test
  @DisplayName("passwordEncoder bean is BCryptPasswordEncoder and hashes non-plaintext values")
  void passwordEncoder_isBCryptPasswordEncoder() {
    UserService userService = org.mockito.Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    BCryptPasswordEncoder encoder = config.passwordEncoder();

    String raw = "Password123!";
    String hash = encoder.encode(raw);

    assertTrue(encoder.matches(raw, hash), "BCrypt encoder should validate its own hash");
    assertNotEquals(raw, hash, "BCrypt hash must not equal raw password (no-op encoding removed)");
  }

  @Test
  @DisplayName("userDetailsServiceBean still returns injected UserService")
  void userDetailsServiceBean_returnsInjectedUserService() {
    UserService userService = org.mockito.Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    var serviceFromBean = config.userDetailsServiceBean();

    org.junit.jupiter.api.Assertions.assertSame(
        userService, serviceFromBean, "UserService wiring must remain unchanged");
  }

  @Test
  @DisplayName("authenticationManager method remains callable with AuthenticationConfiguration")
  void authenticationManager_obtainable() throws Exception {
    UserService userService = org.mockito.Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    AuthenticationConfiguration authenticationConfiguration =
        org.mockito.Mockito.mock(AuthenticationConfiguration.class);

    config.authenticationManager(authenticationConfiguration);
    org.mockito.Mockito.verify(authenticationConfiguration).getAuthenticationManager();
  }
}
