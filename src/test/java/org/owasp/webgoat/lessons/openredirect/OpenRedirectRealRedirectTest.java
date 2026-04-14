package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Test file path (derived):
 * src/test/java/org/owasp/webgoat/lessons/openredirect/OpenRedirectRealRedirectTest.java
 *
 * Delta tests for OpenRedirectRealRedirect focusing on validation of redirect targets:
 * - external or malformed URLs must be rejected and redirected to a safe internal path;
 * - valid internal paths (starting with '/') remain allowed.
 */
class OpenRedirectRealRedirectTest {

  @Test
  void real_redirectsToSafeHomeForExternalUrl() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mav = controller.real("http://evil.com");

    assertEquals("redirect:/home", mav.getViewName());
  }

  @Test
  void real_redirectsToSafeHomeForSchemeRelativeUrl() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mav = controller.real("//evil.com");

    assertEquals("redirect:/home", mav.getViewName());
  }

  @Test
  void real_allowsInternalRelativePath() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mav = controller.real("/account/profile");

    assertEquals(
        "redirect:/account/profile",
        mav.getViewName(),
        "Internal paths starting with '/' must remain allowed");
  }
}
