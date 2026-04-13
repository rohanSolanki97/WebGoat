package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delta tests for OpenRedirectRealRedirect focusing on URL validation:
 * - Safe relative URLs are allowed.
 * - External / protocol-relative / scheme-based URLs are rejected and redirected
 *   to /welcome.mvc.
 */
class OpenRedirectRealRedirectTest {

  private final OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

  @Test
  void real_allowsSafeRelativeUrl() {
    ModelAndView mav = controller.real("/some/page");

    assertEquals("redirect:/some/page", mav.getViewName());
  }

  @Test
  void real_rejectsAbsoluteHttpUrl() {
    ModelAndView mav = controller.real("http://evil.com");

    assertEquals("redirect:/welcome.mvc", mav.getViewName());
  }

  @Test
  void real_rejectsProtocolRelativeUrl() {
    ModelAndView mav = controller.real("//evil.com");

    assertEquals("redirect:/welcome.mvc", mav.getViewName());
  }

  @Test
  void real_rejectsJavascriptScheme() {
    ModelAndView mav = controller.real("javascript:alert(1)");

    assertEquals("redirect:/welcome.mvc", mav.getViewName());
  }

  @Test
  void real_rejectsBackslashInUrl() {
    ModelAndView mav = controller.real("/path\\to\\something");

    assertEquals("redirect:/welcome.mvc", mav.getViewName());
  }
}
