package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/*
 * Delta tests for:
 *   Source: src/main/java/org/owasp/webgoat/lessons/openredirect/OpenRedirectRealRedirect.java
 *   Test:   src/test/java/org/owasp/webgoat/lessons/openredirect/OpenRedirectRealRedirectTest.java
 *
 * Focus: redirect target validation — only safe internal paths allowed; unsafe URLs redirected to '/'.
 */
public class OpenRedirectRealRedirectTest {

  private final OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

  @Test
  void realShouldAllowSafeInternalPath() {
    // Arrange
    String url = "/internal/page";

    // Act
    ModelAndView mav = controller.real(url);

    // Assert: redirect to the same internal path is allowed
    assertEquals("redirect:/internal/page", mav.getViewName());
  }

  @Test
  void realShouldRejectExternalUrlWithScheme() {
    // Arrange
    String url = "http://evil.com/phish";

    // Act
    ModelAndView mav = controller.real(url);

    // Assert: external absolute URL is not used; redirected to safe default
    assertEquals("redirect:/", mav.getViewName());
  }

  @Test
  void realShouldRejectSchemeRelativeUrl() {
    // Arrange
    String url = "//evil.com/redirect";

    // Act
    ModelAndView mav = controller.real(url);

    // Assert
    assertEquals("redirect:/", mav.getViewName());
  }

  @Test
  void realShouldRejectBackslashInUrl() {
    // Arrange
    String url = "/\\evil";

    // Act
    ModelAndView mav = controller.real(url);

    // Assert
    assertEquals("redirect:/", mav.getViewName());
  }

  @Test
  void realShouldRejectEmptyOrNullUrls() {
    // empty string
    assertEquals("redirect:/", controller.real(" ").getViewName());
  }
}
