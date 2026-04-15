package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for BlindSendFileAssignment focusing on the changed behavior:
 * - Username is sanitized using FilenameUtils.getName when constructing the XXE directory.
 * - The resulting path is built via Paths.get(...).normalize().
 *
 * Test file path (derived): src/test/java/org/owasp/webgoat/lessons/xxe/BlindSendFileAssignmentTest.java
 */
public class BlindSendFileAssignmentTest {

  private File tempBaseDir;

  @BeforeEach
  void setUp() throws Exception {
    tempBaseDir = Files.createTempDirectory("webgoat-xxe-test").toFile();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (tempBaseDir != null && tempBaseDir.exists()) {
      deleteRecursively(tempBaseDir);
    }
  }

  @Test
  void initialize_preventsDirectoryTraversalInUsername() {
    String baseDir = tempBaseDir.getAbsolutePath();
    CommentsCache commentsCache = new CommentsCache(); // harmless cache for test
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = org.mockito.Mockito.mock(WebGoatUser.class);
    when(user.getUsername()).thenReturn("../evilUser");

    assignment.initialize(user);

    String sanitized = FilenameUtils.getName("../evilUser");
    Path expectedPath = Paths.get(baseDir, "XXE", sanitized).normalize();
    File expectedDir = expectedPath.toFile();

    assertTrue(
        expectedDir.exists() && expectedDir.isDirectory(),
        "Sanitized user directory should exist under the XXE folder");

    File traversalDir = new File(baseDir, "/XXE/../evilUser");
    assertFalse(
        traversalDir.exists(),
        "Raw traversal-based directory must not be created after sanitization");
  }

  @Test
  void initialize_createsDirectoryForSimpleUsername() {
    String baseDir = tempBaseDir.getAbsolutePath();
    CommentsCache commentsCache = new CommentsCache();
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = org.mockito.Mockito.mock(WebGoatUser.class);
    when(user.getUsername()).thenReturn("alice");

    assignment.initialize(user);

    Path expectedPath = Paths.get(baseDir, "XXE", "alice").normalize();
    File expectedDir = expectedPath.toFile();

    assertTrue(
        expectedDir.exists() && expectedDir.isDirectory(),
        "Directory for simple username should be created directly under XXE");
  }

  private void deleteRecursively(File file) {
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
