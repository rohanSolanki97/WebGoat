package org.owasp.webgoat.lessons.openredirect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delta test for OpenRedirectRealRedirect (BATCH-008)
 * Path: src/test/java/org/owasp/webgoat/lessons/openredirect/OpenRedirectRealRedirectTest.java
 *
 * Focus: ensure redirects are only allowed to validated internal URLs and that
 * untrusted external or traversal URLs are rejected and redirected to /welcome.mvc.
 */
class OpenRedirectRealRedirectTest {

  @Test
  void real_shouldAllowSafeInternalUrl() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String safeUrl = "/internal/page";

    // Act
    ModelAndView mav = controller.real(safeUrl);

    // Assert
    assertThat(mav.getViewName()).isEqualTo("redirect:" + safeUrl);
  }

  @Test
  void real_shouldRejectExternalUrlAndFallbackToWelcome() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String maliciousUrl = "https://evil.com/phish";

    // Act
    ModelAndView mav = controller.real(maliciousUrl);

    // Assert
    assertThat(mav.getViewName()).isEqualTo("redirect:/welcome.mvc");
  }

  @Test
  void real_shouldRejectTraversalUrlAndFallbackToWelcome() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String traversalUrl = "/../admin";

    // Act
    ModelAndView mav = controller.real(traversalUrl);

    // Assert
    assertThat(mav.getViewName()).isEqualTo("redirect:/welcome.mvc");
  }

  @Test
  void real_shouldRejectEmptyUrlAndFallbackToWelcome() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String emptyUrl = "";

    // Act
    ModelAndView mav = controller.real(emptyUrl);

    // Assert
    assertThat(mav.getViewName()).isEqualTo("redirect:/welcome.mvc");
  }
}
