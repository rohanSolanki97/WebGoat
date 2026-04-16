package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delta tests for OpenRedirectRealRedirect focusing on:
 * - preventing open redirects to external domains
 * - still allowing safe internal redirects
 */
public class OpenRedirectRealRedirectTest {

  @Test
  void real_withExternalUrl_redirectsToSafeDefault() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String externalUrl = "http://attacker.example.com";

    // Act
    ModelAndView mav = controller.real(externalUrl);

    // Assert
    assertEquals(
        "redirect:/welcome.mvc",
        mav.getViewName(),
        "External URLs must not be used directly; should fall back to safe internal page");
  }

  @Test
  void real_withInternalPath_redirectsAsRequested() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String internalPath = "/profile";

    // Act
    ModelAndView mav = controller.real(internalPath);

    // Assert
    assertEquals("redirect:/profile", mav.getViewName());
  }

  @Test
  void real_withMalformedInternalLikeUrl_redirectsToSafeDefault() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String trickyUrl = "//evil.com";

    // Act
    ModelAndView mav = controller.real(trickyUrl);

    // Assert
    assertEquals(
        "redirect:/welcome.mvc",
        mav.getViewName(),
        "URLs starting with '//' must not be treated as internal paths");
  }
}
