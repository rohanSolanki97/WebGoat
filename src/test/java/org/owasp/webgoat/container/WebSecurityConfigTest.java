package org.owasp.webgoat.container;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Delta tests for WebSecurityConfig focusing on:
 * - use of a secure PasswordEncoder instead of NoOpPasswordEncoder
 * - CSRF protection being enabled via CookieCsrfTokenRepository.
 */
class WebSecurityConfigTest {

  @Test
  void passwordEncoder_shouldNotReturnPlainTextAndShouldValidatePassword() {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    PasswordEncoder encoder = config.passwordEncoder();
    String raw = "password123";
    String encoded = encoder.encode(raw);

    // Assert: encoded value differs from raw and matches via PasswordEncoder API
    org.junit.jupiter.api.Assertions.assertNotEquals(raw, encoded);
    org.junit.jupiter.api.Assertions.assertTrue(encoder.matches(raw, encoded));
  }

  @Test
  void configureGlobal_shouldRegisterPasswordEncoderWithAuthenticationManagerBuilder()
      throws Exception {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    AuthenticationManagerBuilder authBuilder =
        Mockito.mock(AuthenticationManagerBuilder.class);
    Mockito
        .when(authBuilder.userDetailsService(Mockito.any(UserDetailsService.class)))
        .thenReturn(authBuilder);

    // Act
    config.configureGlobal(authBuilder);

    // Assert: passwordEncoder() is wired into AuthenticationManagerBuilder
    Mockito.verify(authBuilder).userDetailsService(userService);
    Mockito.verify(authBuilder)
        .passwordEncoder(Mockito.any(PasswordEncoder.class));
  }

  @Test
  void filterChain_shouldBeBuildableWithCsrfConfigured() throws Exception {
    // This test ensures that enabling CSRF with CookieCsrfTokenRepository produces
    // a valid SecurityFilterChain configuration (i.e., no misconfiguration exceptions).
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    HttpSecurity http =
        new HttpSecurity(null, null, java.util.List.of(), null, null, null, null);

    SecurityFilterChain chain = config.filterChain(http);

    org.junit.jupiter.api.Assertions.assertNotNull(chain);
  }
}
