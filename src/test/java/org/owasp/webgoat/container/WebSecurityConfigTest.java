package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class WebSecurityConfigTest {

  @Test
  void passwordEncoder_returnsBCryptPasswordEncoderAndHashesPassword() {
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    PasswordEncoder encoder = config.passwordEncoder();

    assertTrue(
        encoder instanceof BCryptPasswordEncoder,
        "passwordEncoder bean must be an instance of BCryptPasswordEncoder");

    String rawPassword = "Secret123!";
    String encoded = encoder.encode(rawPassword);

    assertNotEquals(rawPassword, encoded);
    assertTrue(encoder.matches(rawPassword, encoded));
  }
}
