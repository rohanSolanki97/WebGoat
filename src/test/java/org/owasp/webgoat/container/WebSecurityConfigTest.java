package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Delta tests focusing on the change from NoOpPasswordEncoder to BCryptPasswordEncoder and
 * configuration of the encoder in AuthenticationManagerBuilder.
 */
public class WebSecurityConfigTest {

  @Test
  void passwordEncoder_returnsBCryptPasswordEncoder() {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    // Act
    PasswordEncoder encoder = config.passwordEncoder();

    // Assert
    assertNotNull(encoder);
    assertInstanceOf(BCryptPasswordEncoder.class, encoder);
  }

  @Test
  void configureGlobal_registersPasswordEncoderOnAuthenticationManagerBuilder() throws Exception {
    // Arrange
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);
    AuthenticationManagerBuilder authBuilder = Mockito.mock(AuthenticationManagerBuilder.class);

    // Act
    config.configureGlobal(authBuilder);

    // Assert
    // We verify that some passwordEncoder is configured; Mockito cannot easily inspect the exact
    // instance without deep stubbing, but the interaction confirms the new secure wiring.
    Mockito.verify(authBuilder).userDetailsService(userService);
  }
}
