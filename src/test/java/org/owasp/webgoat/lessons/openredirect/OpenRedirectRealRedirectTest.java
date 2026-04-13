package org.owasp.webgoat.lessons.openredirect;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Delta tests for OpenRedirectRealRedirect focusing on validation of the 'url' parameter and the
 * safe fallback redirect.
 */
class OpenRedirectRealRedirectTest {

  @Test
  void real_shouldRedirectToSafeDefaultOnExternalUrl() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mv = controller.real("http://evil.com");

    org.junit.jupiter.api.Assertions.assertEquals("redirect:/welcome.mvc", mv.getViewName());
  }

  @Test
  void real_shouldRedirectToSafeDefaultOnPathTraversal() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mv = controller.real("/../admin");

    org.junit.jupiter.api.Assertions.assertEquals("redirect:/welcome.mvc", mv.getViewName());
  }

  @Test
  void real_shouldAllowSimpleInternalPath() {
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    ModelAndView mv = controller.real("/internal/page");

    org.junit.jupiter.api.Assertions.assertEquals("redirect:/internal/page", mv.getViewName());
  }
}
