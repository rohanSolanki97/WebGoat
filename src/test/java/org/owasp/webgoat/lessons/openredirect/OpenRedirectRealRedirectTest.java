package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delta tests for OpenRedirectRealRedirect focusing on:
 * - Restricting redirects to safe relative paths.
 * - Rejecting external URLs containing schemes like http:// or https://.
 *
 * Derived path:
 * src/test/java/org/owasp/webgoat/lessons/openredirect/OpenRedirectRealRedirectTest.java
 */
public class OpenRedirectRealRedirectTest {

  private final OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

  @Test
  void real_shouldRedirectToSafeDefaultForExternalUrl() {
    // Arrange
    String external = "http://evil.com/phish";

    // Act
    ModelAndView mav = controller.real(external);

    // Assert
    assertEquals("redirect:/welcome.mvc", mav.getViewName());
  }

  @Test
  void real_shouldRedirectToSafeDefaultForInvalidOrEmptyUrl() {
    // Arrange
    String empty = "   ";

    // Act
    ModelAndView mav = controller.real(empty);

    // Assert
    assertEquals("redirect:/welcome.mvc", mav.getViewName());
  }

  @Test
  void real_shouldAllowRelativeInternalPathWithoutScheme() {
    // Arrange
    String internal = "/lesson/1";

    // Act
    ModelAndView mav = controller.real(internal);

    // Assert
    assertEquals("redirect:/lesson/1", mav.getViewName());
  }

  @Test
  void real_shouldRejectUrlContainingSchemeDelimiter() {
    // Arrange
    String tricky = "/some/path/http://example.com";

    // Act
    ModelAndView mav = controller.real(tricky);

    // Assert
    // Because it contains '://', even though it starts with '/', it should be rejected
    assertEquals("redirect:/welcome.mvc", mav.getViewName());
  }
}
