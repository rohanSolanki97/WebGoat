package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

/*
 * Delta tests for:
 *   Source: src/main/java/org/owasp/webgoat/lessons/pathtraversal/ProfileUploadBase.java
 *   Test:   src/test/java/org/owasp/webgoat/lessons/pathtraversal/ProfileUploadBaseTest.java
 *
 * Focus: filename sanitization using FilenameUtils.getName to prevent path traversal.
 */
public class ProfileUploadBaseTest {

  @Test
  void executeShouldStripPathComponentsFromFullName() throws Exception {
    // Arrange
    String baseDir = System.getProperty("java.io.tmpdir") + "/webgoat-pt";
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    Mockito.when(file.getBytes()).thenReturn("dummy".getBytes());

    String username = "user1";
    // Attempted path traversal in filename
    String fullName = "../outside/../evil.jpg";

    // Clean any leftovers from previous runs
    File root = new File(baseDir);
    if (root.exists()) {
      FileSystemUtils.deleteRecursively(root);
    }

    // Act
    AttackResult result = base.execute(file, fullName, username);

    // Assert: file must be created within user's PathTraversal directory with sanitized name
    File uploadDir = new File(baseDir, "/PathTraversal/" + username);
    File[] files = uploadDir.listFiles();
    assertTrue(
        files != null && files.length == 1,
        "Exactly one file should be stored in the user upload directory");
    assertEquals(
        "evil.jpg",
        files[0].getName(),
        "Path components must be stripped; only base filename should be used");
  }

  @Test
  void executeShouldNotCreateFilesOutsideUserDirectory() throws Exception {
    // Arrange
    String baseDir = System.getProperty("java.io.tmpdir") + "/webgoat-pt2";
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    Mockito.when(file.getBytes()).thenReturn("dummy".getBytes());

    String username = "user2";
    String fullName = "../../etc/passwd";

    File outside = new File("/etc/passwd"); // Just used for name comparison, not real write

    // Clean prior data
    File root = new File(baseDir);
    if (root.exists()) {
      FileSystemUtils.deleteRecursively(root);
    }

    // Act
    base.execute(file, fullName, username);

    // Assert: no file with the dangerous name appears outside the upload root
    File uploadDir = new File(baseDir, "/PathTraversal/" + username);
    File[] files = uploadDir.listFiles();
    assertTrue(files != null && files.length == 1);
    // Only filename, not full path
    assertEquals(outside.getName(), files[0].getName());
    // And its parent is the expected per-user directory
    assertEquals(uploadDir.getCanonicalPath(), files[0].getParentFile().getCanonicalPath());
  }
}
