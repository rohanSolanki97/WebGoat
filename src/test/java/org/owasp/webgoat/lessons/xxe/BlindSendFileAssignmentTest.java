package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for BlindSendFileAssignment focusing on the secure path construction
 * inside createSecretFileWithRandomContents: use of a fixed base directory, path
 * normalization, and rejection of traversal in usernames.
 */
public class BlindSendFileAssignmentTest {

  @Test
  void createSecretFileWithRandomContents_shouldCreateDirectoryUnderFixedBaseForNormalUser()
      throws Exception {
    // Arrange
    String baseDir = Files.createTempDirectory("webgoat-blind-xxe-").toString();
    CommentsCache comments = mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, comments);

    WebGoatUser user = mock(WebGoatUser.class);
    // Normal username without traversal
    org.mockito.Mockito.when(user.getUsername()).thenReturn("alice");

    // Act
    // We cannot call private method directly, but initialize(user) triggers it
    assignment.initialize(user);

    // Assert: verify that a directory "XXE/alice" is used as the target base
    Path expectedBase = Paths.get(baseDir, "XXE").toAbsolutePath().normalize();
    Path expectedUserDir = expectedBase.resolve("alice").normalize();

    // Capture side effect by checking the filesystem
    // Secret file should exist under the sanitized, normalized user directory
    Path secretFile = expectedUserDir.resolve("secret.txt");
    // If the code has respected the base + sanitized username + startsWith check, the file will be here
    java.nio.file.Files.readString(secretFile); // Will throw if file does not exist
  }

  @Test
  void createSecretFileWithRandomContents_shouldNotCreateDirectoryOutsideBaseForTraversalUser()
      throws Exception {
    // Arrange
    String baseDir = Files.createTempDirectory("webgoat-blind-xxe-").toString();
    CommentsCache comments = mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, comments);

    WebGoatUser maliciousUser = mock(WebGoatUser.class);
    // Username attempting traversal; sanitization + startsWith must prevent escape
    org.mockito.Mockito.when(maliciousUser.getUsername()).thenReturn("../evil");

    // Act
    assignment.initialize(maliciousUser);

    // Assert: ensure we did not create a directory outside of the intended base
    Path baseUserSecretsDir = Paths.get(baseDir, "XXE").toAbsolutePath().normalize();
    Path outsideAttempt = baseUserSecretsDir.getParent().resolve("evil").normalize();

    // The implementation logs and returns early on invalid/traversal usernames,
    // so there must be no "evil/secret.txt" one directory above baseUserSecretsDir.
    Path forbiddenSecret = outsideAttempt.resolve("secret.txt");
    boolean exists = java.nio.file.Files.exists(forbiddenSecret);
    org.junit.jupiter.api.Assertions.assertFalse(
        exists,
        "Path traversal via username must not cause secret.txt to be written outside the XXE base directory");
  }
}
