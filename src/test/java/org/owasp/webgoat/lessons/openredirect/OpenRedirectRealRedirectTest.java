package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delta tests for OpenRedirectRealRedirect ensuring that:
 * - External or malformed URLs are redirected to a safe default ('/').
 * - Valid internal relative paths are still allowed.
 */
public class OpenRedirectRealRedirectTest {

  @Test
  @DisplayName("real redirects external URL to safe root instead of open redirect")
  void real_rejectsExternalUrl() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mv = controller.real("http://evil.com/phish");

    assertEquals("redirect:/", mv.getViewName(), "External URLs must be rejected to '/'");
  }

  @Test
  @DisplayName("real rejects traversal attempts and redirects to root")
  void real_rejectsTraversalUrl() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mv = controller.real("/../admin");

    assertEquals("redirect:/", mv.getViewName(), "Traversal patterns must be rejected to '/'");
  }

  @Test
  @DisplayName("real allows safe internal relative path")
  void real_allowsInternalPath() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mv = controller.real("/welcome.mvc");

    assertTrue(
        mv.getViewName().startsWith("redirect:/welcome.mvc"),
        "Safe internal paths should be allowed");
  }
}
