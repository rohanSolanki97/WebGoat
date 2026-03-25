package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delta tests for path traversal fix in ProfileUploadBase.execute:
 * - Rejects ".." and absolute paths
 * - Enforces canonical path containment within the user's upload directory
 */
class ProfileUploadBaseTest {

  @TempDir Path tempDir;

  @Test
  void execute_rejectsDotDotInFilename() throws Exception {
    // Arrange
    ProfileUploadBase sut = new ProfileUploadBase(tempDir.toString());
    MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getBytes()).thenReturn("x".getBytes());

    // Act
    var result = sut.execute(file, "../evil.txt", "alice");

    // Assert
    assertFalse(result.succeeded(), "Traversal filename should be rejected");
  }

  @Test
  void execute_rejectsAbsoluteFilename() throws Exception {
    // Arrange
    ProfileUploadBase sut = new ProfileUploadBase(tempDir.toString());
    MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getBytes()).thenReturn("x".getBytes());

    String absolute = new File(tempDir.toFile(), "abs.txt").getAbsolutePath();

    // Act
    var result = sut.execute(file, absolute, "alice");

    // Assert
    assertFalse(result.succeeded(), "Absolute filename should be rejected");
  }

  @Test
  void execute_allowsSafeFilenameAndWritesWithinUploadDirectory() throws Exception {
    // Arrange
    ProfileUploadBase sut = new ProfileUploadBase(tempDir.toString());
    MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getBytes()).thenReturn("hello".getBytes());

    // Act
    var result = sut.execute(file, "profile.jpg", "alice");

    // Assert
    // The method may return success/failed depending on lesson logic; we assert the security property:
    // file must be created under /PathTraversal/alice
    Path expectedDir = tempDir.resolve("PathTraversal").resolve("alice");
    Path expectedFile = expectedDir.resolve("profile.jpg");
    assertTrue(Files.exists(expectedFile), "Expected uploaded file to be written inside user directory");
    assertTrue(
        expectedFile.toRealPath().startsWith(expectedDir.toRealPath()),
        "Uploaded file must remain within the canonical upload directory");
  }
}
