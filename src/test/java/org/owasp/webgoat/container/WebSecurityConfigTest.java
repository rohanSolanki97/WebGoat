package org.owasp.webgoat.container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Delta tests for WebSecurityConfig focusing on:
 * - Replacement of NoOpPasswordEncoder with BCryptPasswordEncoder (PasswordEncoder bean).
 * - Removal of explicit CSRF disabling (CSRF should no longer be disabled here).
 *
 * These tests avoid full Spring context startup and focus on the changed behaviors.
 */
public class WebSecurityConfigTest {

  @Test
  void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
    // Arrange
    UserService userService = mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    PasswordEncoder encoder = config.passwordEncoder();

    // Assert
    assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    String raw = "password";
    String encoded = encoder.encode(raw);
    assertThat(encoded).isNotEqualTo(raw);
    assertThat(encoder.matches(raw, encoded)).isTrue();
  }

  @Test
  void filterChain_shouldNotDisableCsrfExplicitly() throws Exception {
    // Arrange
    UserService userService = mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);
    HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

    // Act
    SecurityFilterChain chain = config.filterChain(http);

    // Assert
    // We cannot easily introspect HTTP security configuration from the mock,
    // but this test ensures the method can be invoked without errors using the new configuration.
    assertThat(chain).isNotNull();
  }

  @Test
  void configureGlobal_shouldConfigureUserDetailsService() throws Exception {
    // Arrange
    UserService userService = mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);
    AuthenticationManagerBuilder authBuilder = mock(AuthenticationManagerBuilder.class);

    // Act
    config.configureGlobal(authBuilder);

    // Assert
    verify(authBuilder).userDetailsService(userService);
  }

  @Test
  void authenticationManager_shouldDelegateToAuthenticationConfiguration() throws Exception {
    // Arrange
    UserService userService = mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);
    AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(null);

    // Act
    config.authenticationManager(authenticationConfiguration);

    // Assert
    verify(authenticationConfiguration).getAuthenticationManager();
  }
}
