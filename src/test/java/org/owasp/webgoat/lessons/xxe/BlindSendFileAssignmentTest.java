package org.owasp.webgoat.lessons.xxe;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.container.lessons.Initializable;
import org.owasp.webgoat.container.users.WebGoatUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Delta tests for BlindSendFileAssignment focusing on secure path construction using
 * java.nio.file.Path/Paths and the startsWith() check.
 */
class BlindSendFileAssignmentTest {

  private static final String BASE_DIR = "build/tmp/webgoatHome";

  @AfterEach
  void cleanup() throws IOException {
    // Clean up the temporary base directory after each test run
    Path base = Paths.get(BASE_DIR);
    if (Files.exists(base)) {
      Files.walk(base)
          .sorted((a, b) -> b.compareTo(a))
          .forEach(
              p -> {
                try {
                  Files.deleteIfExists(p);
                } catch (IOException ignored) {
                  // ignore
                }
              });
    }
  }

  @Test
  void initialize_shouldCreateSecretFileWithinUserDirectory() throws Exception {
    CommentsCache commentsCache = mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(BASE_DIR, commentsCache);

    WebGoatUser user = mock(WebGoatUser.class);
    when(user.getUsername()).thenReturn("alice");

    assignment.initialize(user);

    Path userDir = Paths.get(BASE_DIR, "XXE", "alice").normalize();
    Path secretFile = userDir.resolve("secret.txt").normalize();

    org.junit.jupiter.api.Assertions.assertTrue(Files.exists(secretFile));
    org.junit.jupiter.api.Assertions.assertTrue(secretFile.startsWith(userDir));
    String contents = Files.readString(secretFile, StandardCharsets.UTF_8);
    org.junit.jupiter.api.Assertions.assertTrue(contents.startsWith("WebGoat 8.0 rocks..."));
  }

  @Test
  void initialize_shouldNotWriteOutsideBaseDirectoryOnMaliciousUsername() throws Exception {
    CommentsCache commentsCache = mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(BASE_DIR, commentsCache);

    WebGoatUser user = mock(WebGoatUser.class);
    // Attempt directory traversal via username should not escape base dir
    when(user.getUsername()).thenReturn("../../evil");

    assignment.initialize(user);

    Path basePath = Paths.get(BASE_DIR).normalize();
    // Ensure no "evil" directory was created outside the expected XXE tree
    Path evilPath = basePath.getParent() == null ? basePath : basePath.getParent().resolve("evil");
    org.junit.jupiter.api.Assertions.assertFalse(Files.exists(evilPath));
  }
}
