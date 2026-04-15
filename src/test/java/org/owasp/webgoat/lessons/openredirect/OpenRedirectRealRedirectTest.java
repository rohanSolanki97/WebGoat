// File: src/test/java/org/owasp/webgoat/lessons/openredirect/OpenRedirectRealRedirectTest.java
package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class OpenRedirectRealRedirectTest {

  @Test
  void real_redirectsToSafeDefaultWhenUrlIsExternal() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String externalUrl = "http://evil.com/phishing";

    // Act
    ModelAndView mav = controller.real(externalUrl);

    // Assert
    // External URLs must not be used; they should be normalized to a safe default
    assertEquals("redirect:/", mav.getViewName());
  }

  @Test
  void real_allowsInternalRelativeRedirects() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String internalUrl = "/internal/page";

    // Act
    ModelAndView mav = controller.real(internalUrl);

    // Assert
    // Relative paths starting with '/' are allowed
    assertEquals("redirect:" + internalUrl, mav.getViewName());
  }

  @Test
  void real_handlesBlankOrNullUrlByRedirectingToRoot() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();

    // Act
    ModelAndView mavBlank = controller.real("   ");
    ModelAndView mavNull = controller.real(null);

    // Assert
    assertEquals("redirect:/", mavBlank.getViewName());
    assertEquals("redirect:/", mavNull.getViewName());
  }
}
