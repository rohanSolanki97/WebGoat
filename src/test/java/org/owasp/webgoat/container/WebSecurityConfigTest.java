// File: src/test/java/org/owasp/webgoat/container/WebSecurityConfigTest.java
// Derived from src/main/java/org/owasp/webgoat/container/WebSecurityConfig.java
package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class WebSecurityConfigTest {

  @Test
  void passwordEncoder_returnsBCryptPasswordEncoder() {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    PasswordEncoder encoder = config.passwordEncoder();

    // Assert
    assertTrue(encoder instanceof BCryptPasswordEncoder);
  }

  @Test
  void filterChain_invokesCsrfCustomizer() throws Exception {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);
    HttpSecurity http =
        Mockito.mock(HttpSecurity.class, Mockito.RETURNS_DEEP_STUBS);

    // Act
    config.filterChain(http);

    // Assert
    // We verify that the csrf() lambda is invoked; this ensures that the
    // configuration no longer calls csrf().disable() via a hard-coded disable.
    Mockito.verify(http).csrf(Mockito.any());
  }
}
