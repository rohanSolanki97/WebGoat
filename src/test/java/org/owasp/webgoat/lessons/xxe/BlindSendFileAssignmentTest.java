package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.WebGoatUser;
import org.springframework.beans.factory.annotation.Value;

/**
 * Delta tests for BlindSendFileAssignment focusing on the sanitized directory construction
 * in createSecretFileWithRandomContents() after the path traversal fix.
 */
class BlindSendFileAssignmentTest {

  // Minimal CommentsCache stub to satisfy constructor; behavior is not under test here.
  private static class NoOpCommentsCache extends CommentsCache {
    // TODO: Provide minimal implementation if needed by the actual CommentsCache base class.
  }

  @Test
  void createSecretFileWithRandomContents_usesSanitizedUsernameAndCreatesSecretFile()
      throws Exception {
    File tempRoot = Files.createTempDirectory("webgoat-home-").toFile();
    try {
      String webGoatHomeDirectory = tempRoot.getAbsolutePath();
      CommentsCache comments = new NoOpCommentsCache();

      BlindSendFileAssignment assignment =
          new BlindSendFileAssignment(webGoatHomeDirectory, comments);

      WebGoatUser user = new WebGoatUser();
      // Username contains path traversal and slashes; only the last segment must be used.
      user.setUsername("../some/../../evil");

      assignment.initialize(user);

      File expectedDir = new File(webGoatHomeDirectory, "/XXE/evil");
      assertTrue(expectedDir.exists(), "Sanitized user directory should exist under /XXE/evil");

      File[] files = expectedDir.listFiles();
      assertTrue(files != null && files.length == 1, "Exactly one file should be created");
      File secretFile = files[0];

      assertEquals("secret.txt", secretFile.getName(), "Secret file must be named secret.txt");
      assertTrue(secretFile.isFile(), "secret.txt must be a regular file");
    } finally {
      // Cleanup temp directory
      deleteRecursively(tempRoot);
    }
  }

  private static void deleteRecursively(File file) {
    if (file == null || !file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          deleteRecursively(child);
        }
      }
    }
    file.delete();
  }
}
