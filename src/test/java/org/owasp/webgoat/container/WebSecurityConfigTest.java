package org.owasp.webgoat.container;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * File path (derived from main source): src/test/java/org/owasp/webgoat/container/WebSecurityConfigTest.java
 *
 * Delta tests for WebSecurityConfig verifying that:
 * - BCryptPasswordEncoder is used instead of NoOpPasswordEncoder,
 * - AuthenticationManagerBuilder is wired with passwordEncoder(),
 * - CSRF configuration is enabled with CookieCsrfTokenRepository.
 */
class WebSecurityConfigTest {

  @Test
  void passwordEncoder_isBCryptPasswordEncoder() {
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    PasswordEncoder encoder = config.passwordEncoder();

    assertInstanceOf(
        BCryptPasswordEncoder.class,
        encoder,
        "Password encoder must be BCryptPasswordEncoder after the fix");
  }

  @Test
  void configureGlobal_usesProvidedPasswordEncoder() throws Exception {
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    AuthenticationManagerBuilder authBuilder = Mockito.mock(AuthenticationManagerBuilder.class);
    PasswordEncoder encoder = config.passwordEncoder();

    Mockito.when(authBuilder.userDetailsService(Mockito.any(UserDetailsService.class)))
        .thenReturn(authBuilder);

    config.configureGlobal(authBuilder);

    Mockito.verify(authBuilder).userDetailsService(userService);
    Mockito.verify(authBuilder).passwordEncoder(encoder);
  }

  @Test
  @SuppressWarnings("unchecked")
  void filterChain_configuresCsrfWithCookieCsrfTokenRepository() throws Exception {
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    HttpSecurity http = Mockito.mock(HttpSecurity.class, Mockito.RETURNS_DEEP_STUBS);

    Mockito.when(http.authorizeHttpRequests(Mockito.any())).thenReturn(http);
    Mockito.when(http.formLogin(Mockito.any())).thenReturn(http);
    Mockito.when(http.oauth2Login(Mockito.any())).thenReturn(http);
    Mockito.when(http.logout(Mockito.any())).thenReturn(http);
    Mockito.when(http.headers(Mockito.any())).thenReturn(http);
    Mockito.when(http.exceptionHandling(Mockito.any())).thenReturn(http);
    Mockito.when(http.build()).thenReturn(Mockito.mock(SecurityFilterChain.class));

    @SuppressWarnings("rawtypes")
    CsrfConfigurer csrfConfigurer = Mockito.mock(CsrfConfigurer.class);
    Mockito.when(http.csrf(Mockito.any())).thenAnswer(invocation -> {
      @SuppressWarnings("rawtypes")
      Consumer<CsrfConfigurer> consumer = invocation.getArgument(0);
      consumer.accept(csrfConfigurer);
      return http;
    });

    config.filterChain(http);

    ArgumentCaptor<CookieCsrfTokenRepository> repoCaptor =
        ArgumentCaptor.forClass(CookieCsrfTokenRepository.class);
    Mockito.verify(csrfConfigurer).csrfTokenRepository(repoCaptor.capture());

    CookieCsrfTokenRepository usedRepo = repoCaptor.getValue();
    // The important property is that the CSRF repository used comes from CookieCsrfTokenRepository,
    // which is what the fix introduced.
    assertInstanceOf(CookieCsrfTokenRepository.class, usedRepo);
  }

  @Test
  void authenticationManager_usesSpringAuthenticationConfiguration() throws Exception {
    UserService userService = Mockito.mock(UserService.class);
    WebSecurityConfig config = new WebSecurityConfig(userService);

    AuthenticationManager mockManager = Mockito.mock(AuthenticationManager.class);
    AuthenticationConfiguration authConfig = Mockito.mock(AuthenticationConfiguration.class);
    Mockito.when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

    AuthenticationManager manager = config.authenticationManager(authConfig);

    assertSame(
        mockManager,
        manager,
        "authenticationManager bean should delegate to AuthenticationConfiguration");
  }
}
