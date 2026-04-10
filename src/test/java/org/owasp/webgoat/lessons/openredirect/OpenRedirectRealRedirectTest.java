package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

class OpenRedirectRealRedirectTest {

  @Test
  void real_allowsRelativeUrlStartingWithSlash() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mv = controller.real("/internal/page");
    assertEquals("redirect:/internal/page", mv.getViewName());
  }

  @Test
  void real_redirectsExternalUrlToRoot() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mv = controller.real("https://attacker.example/phish");
    assertEquals("redirect:/", mv.getViewName());
  }

  @Test
  void real_redirectsNullOrEmptyUrlToRoot() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mvNull = controller.real(null);
    ModelAndView mvEmpty = controller.real("");

    assertEquals("redirect:/", mvNull.getViewName());
    assertEquals("redirect:/", mvEmpty.getViewName());
  }
}
