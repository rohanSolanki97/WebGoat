package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delta tests for ProfileUploadBase focusing on:
 * - Sanitization of fullName and username to prevent path traversal when constructing filesystem paths.
 *
 * Derived path:
 * src/test/java/org/owasp/webgoat/lessons/pathtraversal/ProfileUploadBaseTest.java
 */
public class ProfileUploadBaseTest {

  @Test
  void execute_shouldSanitizeFullNameAndPreventPathTraversal() throws Exception {
    // Arrange
    String baseDir = Files.createTempDirectory("webgoat-path-").toFile().getAbsolutePath();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);
    MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
    org.mockito.Mockito.when(multipartFile.isEmpty()).thenReturn(false);
    org.mockito.Mockito.when(multipartFile.getBytes()).thenReturn("data".getBytes());

    String maliciousFullName = "../evil/../../escape.txt";
    String username = "user1";

    // Act
    base.execute(multipartFile, maliciousFullName, username);

    // Assert
    File userDir = new File(baseDir, "/PathTraversal/" + username);
    assertTrue(userDir.isDirectory());

    File[] files = userDir.listFiles();
    assertTrue(files != null && files.length == 1, "One file should be created in the user directory");
    File uploaded = files[0];

    // The created file name should be sanitized to the last name segment (no ../ parts)
    assertEquals("escape.txt", uploaded.getName());
    // And it must be inside the intended userDir (not traversed outside)
    assertTrue(uploaded.getCanonicalPath().startsWith(userDir.getCanonicalPath()));
  }

  @Test
  void cleanupAndCreateDirectoryForUser_shouldSanitizeUsernameInDirectoryPath() throws Exception {
    // Arrange
    String baseDir = Files.createTempDirectory("webgoat-path-").toFile().getAbsolutePath();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);
    String maliciousUsername = "../admin";

    // Act
    File userDir = base.cleanupAndCreateDirectoryForUser(maliciousUsername);

    // Assert
    // Directory name should be sanitized
    assertEquals("admin", userDir.getName());
    assertTrue(userDir.getCanonicalPath().startsWith(new File(baseDir, "/PathTraversal").getCanonicalPath()));
  }
}
