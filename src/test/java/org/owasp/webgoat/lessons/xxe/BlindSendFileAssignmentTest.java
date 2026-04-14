package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Test file path (derived): src/test/java/org/owasp/webgoat/lessons/xxe/BlindSendFileAssignmentTest.java
 *
 * Delta tests for BlindSendFileAssignment focusing on secure path handling and prevention of
 * path traversal when creating the secret file.
 */
class BlindSendFileAssignmentTest {

  private final File tempRootDir;

  BlindSendFileAssignmentTest() throws Exception {
    this.tempRootDir = Files.createTempDirectory("webgoat-blind-xxe-").toFile();
  }

  @AfterEach
  void cleanup() throws Exception {
    if (tempRootDir.exists()) {
      deleteRecursively(tempRootDir.toPath());
    }
  }

  private void deleteRecursively(Path path) throws Exception {
    if (Files.isDirectory(path)) {
      try (var stream = Files.list(path)) {
        stream.forEach(
            p -> {
              try {
                deleteRecursively(p);
              } catch (Exception ignored) {
              }
            });
      }
    }
    Files.deleteIfExists(path);
  }

  @Test
  void createSecretFileWithRandomContents_staysWithinUserDirectory() throws Exception {
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment =
        new BlindSendFileAssignment(tempRootDir.getAbsolutePath(), commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    Mockito.when(user.getUsername()).thenReturn("alice");

    assignment.initialize(user);

    File userDir = new File(tempRootDir, "/XXE/" + user.getUsername());
    File secretFile = new File(userDir, "secret.txt");

    Path userDirPath = userDir.toPath().toRealPath();
    Path secretPath = secretFile.toPath().toRealPath();

    assertTrue(
        secretPath.startsWith(userDirPath),
        "Secret file must be created inside the per-user XXE directory, preventing traversal");
  }
}
