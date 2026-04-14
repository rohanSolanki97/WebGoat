package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Test file path (derived):
 * src/test/java/org/owasp/webgoat/lessons/pathtraversal/ProfileUploadBaseTest.java
 *
 * Delta tests for ProfileUploadBase focusing on:
 * - sanitization of uploaded filename and anchored path (no traversal),
 * - rejecting attempts to escape the user upload directory.
 */
class ProfileUploadBaseTest {

  private final File tempRootDir;

  ProfileUploadBaseTest() throws Exception {
    this.tempRootDir = Files.createTempDirectory("webgoat-path-").toFile();
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
  void execute_writesFileInsideUserDirectoryForNormalName() throws Exception {
    ProfileUploadBase base = new ProfileUploadBase(tempRootDir.getAbsolutePath());

    MockMultipartFile file =
        new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "dummy".getBytes());

    AttackResult result = base.execute(file, "avatar.jpg", "bob");

    File uploadDir = new File(tempRootDir, "/PathTraversal/bob");
    File storedFile = new File(uploadDir, "avatar.jpg");

    assertTrue(storedFile.exists(), "Uploaded file must be stored inside the user directory");
    assertFalse(
        result.getLessonCompleted(),
        "Normal upload should not inadvertently mark the lesson as completed");
  }

  @Test
  void execute_rejectsPathTraversalInFullName() throws Exception {
    ProfileUploadBase base = new ProfileUploadBase(tempRootDir.getAbsolutePath());

    MockMultipartFile file =
        new MockMultipartFile("file", "evil.jpg", "image/jpeg", "dummy".getBytes());

    String traversalName = "../evil.jpg";

    AttackResult result = base.execute(file, traversalName, "bob");

    assertFalse(
        result.getLessonCompleted(),
        "Traversal attempt must not complete the lesson");
  }
}
