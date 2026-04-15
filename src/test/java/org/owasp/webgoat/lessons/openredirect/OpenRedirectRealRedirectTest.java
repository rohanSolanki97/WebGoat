package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delta tests for OpenRedirectRealRedirect focusing on the changed behavior:
 * - Only internal paths starting with '/' are now used as redirect targets.
 * - All other values are redirected to '/' (safe default).
 */
public class OpenRedirectRealRedirectTest {

  private OpenRedirectRealRedirect controller;

  @BeforeEach
  void setUp() {
    controller = new OpenRedirectRealRedirect();
  }

  @Test
  void real_allowsInternalRelativePath() {
    String internal = "/welcome.mvc";

    ModelAndView mav = controller.real(internal);

    assertEquals(
        "redirect:" + internal,
        mav.getViewName(),
        "Internal application paths should be preserved as redirect targets");
  }

  @Test
  void real_blocksExternalUrl() {
    String external = "http://evil.com/phish";

    ModelAndView mav = controller.real(external);

    assertEquals(
        "redirect:/",
        mav.getViewName(),
        "External URLs must be redirected to the safe default '/' ");
  }

  @Test
  void real_handlesNullUrlSafely() {
    ModelAndView mav = controller.real(null);

    assertEquals(
        "redirect:/",
        mav.getViewName(),
        "Null URL must fall back to safe default '/' ");
  }
}
