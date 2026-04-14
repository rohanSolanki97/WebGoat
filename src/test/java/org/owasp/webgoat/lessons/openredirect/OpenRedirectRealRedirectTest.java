package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Delta tests for OpenRedirectRealRedirect focusing on the new validation:
 * - Only allow redirects to internal paths starting with '/'
 * - Fallback to a safe default route for all other values
 */
public class OpenRedirectRealRedirectTest {

  @Test
  void real_allowsInternalRelativePathRedirects() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String internalUrl = "/lessons/openredirect";

    // Act
    ModelAndView mav = controller.real(internalUrl);

    // Assert
    assertEquals("redirect:" + internalUrl, mav.getViewName());
  }

  @Test
  void real_rejectsExternalUrlAndRedirectsToSafeDefault() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String externalUrl = "http://evil.com/phish";

    // Act
    ModelAndView mav = controller.real(externalUrl);

    // Assert
    // In the fixed implementation, an invalid URL returns a RedirectView to /welcome.mvc
    assertTrue(
        mav.getView() instanceof RedirectView,
        "Expected RedirectView for invalid or external redirect targets");
    RedirectView redirectView = (RedirectView) mav.getView();
    assertEquals(
        "/welcome.mvc",
        redirectView.getUrl(),
        "Invalid redirect target should be normalized to a safe internal URL");
  }

  @Test
  void real_handlesNullOrEmptyUrlSafely() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    // Act
    ModelAndView mav = controller.real(null);

    // Assert
    assertTrue(
        mav.getView() instanceof RedirectView,
        "Null URL should be treated as invalid and redirected to a safe page");
    RedirectView redirectView = (RedirectView) mav.getView();
    assertEquals("/welcome.mvc", redirectView.getUrl());
  }
}
