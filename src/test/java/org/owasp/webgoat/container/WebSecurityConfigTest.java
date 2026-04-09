package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
 * Resolved test path:
 * src/test/java/org/owasp/webgoat/container/WebSecurityConfigTest.java
 */
public class WebSecurityConfigTest {

  @Test
  @DisplayName("passwordEncoder bean returns a BCryptPasswordEncoder (no longer NoOpPasswordEncoder)")
  void passwordEncoder_isBCryptPasswordEncoder() {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    PasswordEncoder encoder = config.passwordEncoder();

    // Assert
    assertTrue(
        encoder instanceof BCryptPasswordEncoder,
        "passwordEncoder should use BCryptPasswordEncoder instead of NoOpPasswordEncoder");
  }

  @Test
  @DisplayName("filterChain no longer globally disables CSRF via csrf().disable()")
  void filterChain_doesNotCallCsrfDisable() throws Exception {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    HttpSecurity http =
        Mockito.mock(HttpSecurity.class, Mockito.RETURNS_DEEP_STUBS);

    // Act
    config.filterChain(http);

    // Assert
    // Indirect verification: ensure we never call the old pattern csrf(csrf -> csrf.disable()).
    Mockito.verify(http, never()).csrf(Mockito.any());
  }
}
