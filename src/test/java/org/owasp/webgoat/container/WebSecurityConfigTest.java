package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class WebSecurityConfigTest {

  @Test
  @DisplayName("passwordEncoder bean should be BCryptPasswordEncoder (no plain-text encoder)")
  void passwordEncoderUsesBCrypt() {
    UserService userService = org.mockito.Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    BCryptPasswordEncoder encoder = config.passwordEncoder();

    assertInstanceOf(
        BCryptPasswordEncoder.class,
        encoder,
        "passwordEncoder bean must use BCryptPasswordEncoder, not NoOpPasswordEncoder or plain text");
  }
}
