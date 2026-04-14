package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for BlindSendFileAssignment focusing on the secure path construction change:
 * - Sanitization of username using FilenameUtils.getName(...)
 * - Use of java.nio.file.Paths.get + resolve for safe path creation
 */
public class BlindSendFileAssignmentTest {

  private Path tempHomeDir;

  @BeforeEach
  void setUp() throws Exception {
    tempHomeDir = Files.createTempDirectory("webgoat-home-");
  }

  @AfterEach
  void tearDown() throws Exception {
    if (tempHomeDir != null) {
      Files.walk(tempHomeDir)
          .sorted((a, b) -> b.compareTo(a))
          .forEach(
              p -> {
                try {
                  Files.deleteIfExists(p);
                } catch (Exception ignored) {
                  // ignore cleanup failures in tests
                }
              });
    }
  }

  @Test
  void createSecretFileWithRandomContents_sanitizesUsernameToPreventPathTraversal() throws Exception {
    // Arrange
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment =
        new BlindSendFileAssignment(tempHomeDir.toString(), commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    // Attempt to inject directory traversal into the username
    Mockito.when(user.getUsername()).thenReturn("../evilUser");

    // Act
    assignment.initialize(user);

    // Assert
    // Secret file must be created under <home>/XXE/<sanitizedUsername>/secret.txt
    File expectedDir = tempHomeDir.resolve("XXE").resolve("evilUser").toFile();
    File secretFile = new File(expectedDir, "secret.txt");

    assertTrue(
        secretFile.exists(),
        "Secret file should be created under a sanitized user directory, not using raw '../evilUser'");
    assertTrue(
        secretFile.getCanonicalPath().startsWith(tempHomeDir.toFile().getCanonicalPath()),
        "Secret file path must remain within the configured webGoat home directory");
  }

  @Test
  void createSecretFileWithRandomContents_usesExpectedDirectoryStructure() throws Exception {
    // Arrange
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment =
        new BlindSendFileAssignment(tempHomeDir.toString(), commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    Mockito.when(user.getUsername()).thenReturn("normalUser");

    // Act
    assignment.initialize(user);

    // Assert
    File secretFile =
        tempHomeDir.resolve("XXE").resolve("normalUser").resolve("secret.txt").toFile();
    assertTrue(secretFile.exists(), "Secret file should be created for a normal username");
    assertEquals(
        "secret.txt", secretFile.getName(), "Created file must retain the expected fixed filename");
  }
}
