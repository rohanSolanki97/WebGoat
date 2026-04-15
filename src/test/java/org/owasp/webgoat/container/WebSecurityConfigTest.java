package org.owasp.webgoat.container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class WebSecurityConfigTest {

  @Test
  public void passwordEncoder_isNotNoOp() {
    UserService userService = mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    PasswordEncoder encoder = config.passwordEncoder();

    String raw = "secretPassword";
    String encoded = encoder.encode(raw);

    assertThat(encoded).isNotEqualTo(raw);
    assertThat(encoder.matches(raw, encoded)).isTrue();
  }

  @Test
  public void userDetailsServiceBean_returnsUserService() {
    UserService userService = mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    UserDetailsService uds = config.userDetailsServiceBean();

    assertThat(uds).isSameAs(userService);
  }

  @Test
  public void authenticationManager_isDelegatedToAuthenticationConfiguration() throws Exception {
    UserService userService = mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    AuthenticationConfiguration authenticationConfiguration =
        mock(AuthenticationConfiguration.class);
    AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

    AuthenticationManager result = config.authenticationManager(authenticationConfiguration);

    assertThat(result).isSameAs(authenticationManager);
  }
}
