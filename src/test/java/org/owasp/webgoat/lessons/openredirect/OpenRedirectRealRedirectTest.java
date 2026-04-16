package org.owasp.webgoat.lessons.openredirect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delta tests for OpenRedirectRealRedirect verifying that:
 * - Only relative, internal URLs are allowed for redirects.
 * - External or absolute URLs are rejected and redirected to a safe default ("/").
 */
public class OpenRedirectRealRedirectTest {

  @Test
  void real_shouldAllowInternalRelativeUrlWithoutScheme() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String url = "/welcome.mvc";

    // Act
    ModelAndView mav = controller.real(url);

    // Assert
    assertThat(mav.getViewName()).isEqualTo("redirect:/welcome.mvc");
  }

  @Test
  void real_shouldRejectExternalUrlContainingScheme() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String externalUrl = "http://evil.com/phish";

    // Act
    ModelAndView mav = controller.real(externalUrl);

    // Assert
    // The controller should redirect to the safe default instead of the external URL.
    assertThat(mav.getViewName()).isEqualTo("redirect:/");
  }

  @Test
  void real_shouldRejectProtocolRelativeUrl() {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    String externalUrl = "//evil.com/phish";

    // Act
    ModelAndView mav = controller.real(externalUrl);

    // Assert
    assertThat(mav.getViewName()).isEqualTo("redirect:/");
  }
}
