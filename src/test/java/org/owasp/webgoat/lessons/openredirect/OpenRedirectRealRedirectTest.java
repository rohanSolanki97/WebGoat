package org.owasp.webgoat.lessons.openredirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for OpenRedirectRealRedirect focusing on the allowed-path whitelist behavior:
 * only whitelisted paths from the user-controlled URL may be used as redirect targets.
 */
public class OpenRedirectRealRedirectTest {

  @Test
  void openRedirect_shouldRedirectToWhitelistedPath() throws Exception {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    WebGoatUser user = new WebGoatUser("alice", "alice", "ROLE_USER");

    // Act
    String viewName = controller.openRedirect("https://example.com/welcome.mvc", user);

    // Assert
    assertEquals(
        "redirect:/welcome.mvc",
        viewName,
        "Whitelisted path '/welcome.mvc' from the URL should be allowed as redirect target");
  }

  @Test
  void openRedirect_shouldFallbackWhenPathNotWhitelisted() throws Exception {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    WebGoatUser user = new WebGoatUser("alice", "alice", "ROLE_USER");

    // Act
    String viewName =
        controller.openRedirect("https://malicious.example.com/steal-credentials", user);

    // Assert
    assertEquals(
        "redirect:/welcome.mvc",
        viewName,
        "Non-whitelisted path must be rejected and redirected to the safe default");
  }

  @Test
  void openRedirect_shouldHandleRelativePathAndEnforceWhitelist() throws Exception {
    // Arrange
    OpenRedirectRealRedirect controller = new OpenRedirectRealRedirect();
    WebGoatUser user = new WebGoatUser("alice", "alice", "ROLE_USER");

    // Act
    String viewName = controller.openRedirect("/login?next=/admin", user);

    // Assert
    // Only the path component '/login' is considered; query is ignored
    assertEquals(
        "redirect:/login",
        viewName,
        "Relative path '/login' should be allowed as it is in the whitelist");
  }
}
