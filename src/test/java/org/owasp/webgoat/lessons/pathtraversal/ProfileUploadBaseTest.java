package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delta tests for ProfileUploadBase focusing on:
 * - Sanitization of filename and username inputs to prevent path traversal.
 * - Canonical path check ensuring uploaded files remain within the user directory.
 */
public class ProfileUploadBaseTest {

  private Path tempHomeDir;

  @BeforeEach
  void setUp() throws Exception {
    tempHomeDir = Files.createTempDirectory("webgoat-profile-home-");
  }

  @AfterEach
  void tearDown() throws Exception {
    if (tempHomeDir != null) {
      FileSystemUtils.deleteRecursively(tempHomeDir);
    }
  }

  @Test
  void execute_sanitizesFilenameToPreventTraversalOutsideUserDirectory() throws Exception {
    // Arrange
    ProfileUploadBase base = new ProfileUploadBase(tempHomeDir.toString());

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    Mockito.when(file.getBytes()).thenReturn("dummy".getBytes());

    // Attempt to write outside the intended directory using '../' in filename
    String maliciousFullName = "../outside.txt";
    String username = "alice";

    // Act
    AttackResult result = base.execute(file, maliciousFullName, username);

    // Assert
    // The canonical check in execute() should detect an attempt to escape uploadDirectory
    assertFalse(
        result.isLessonCompleted(),
        "Uploading with a path-traversal filename should not complete the lesson");
    // The result is expected to indicate failure or path-traversal handling
  }

  @Test
  void execute_createsFileWithinUserDirectoryForNormalFilename() throws Exception {
    // Arrange
    ProfileUploadBase base = new ProfileUploadBase(tempHomeDir.toString());

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    Mockito.when(file.getBytes()).thenReturn("dummy".getBytes());

    String fullName = "profile.jpg";
    String username = "bob";

    // Act
    AttackResult result = base.execute(file, fullName, username);

    // Assert
    File expectedFile =
        tempHomeDir.resolve("PathTraversal").resolve("bob").resolve("profile.jpg").toFile();
    assertTrue(
        expectedFile.exists(),
        "Normal upload should create the file within the sanitized PathTraversal/<username> directory");
    assertFalse(
        result.isLessonCompleted(),
        "Normal profile upload should typically not mark the lesson as completed");
  }

  @Test
  void cleanupAndCreateDirectoryForUser_sanitizesUsername() throws Exception {
    // Arrange
    ProfileUploadBase base = new ProfileUploadBase(tempHomeDir.toString());
    String maliciousUsername = "../attacker";

    // Act
    File uploadDir = base.cleanupAndCreateDirectoryForUser(maliciousUsername);

    // Assert
    assertTrue(
        uploadDir.getCanonicalPath().startsWith(tempHomeDir.toFile().getCanonicalPath()),
        "Directory for a malicious username must be created inside the configured home directory");
    assertFalse(
        uploadDir.getName().contains(".."),
        "Sanitized directory name must not contain path traversal sequences");
  }
}
