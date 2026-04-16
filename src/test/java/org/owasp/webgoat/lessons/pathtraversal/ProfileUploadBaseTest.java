package org.owasp.webgoat.lessons.pathtraversal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delta tests for ProfileUploadBase focusing on the change that sanitizes the
 * user-supplied filename using FilenameUtils.getName to prevent path traversal.
 */
public class ProfileUploadBaseTest {

  @Test
  void execute_shouldStripPathComponentsFromFullNameAndPreventTraversal(@TempDir Path tmpDir)
      throws Exception {
    // Arrange
    String baseDir = tmpDir.toAbsolutePath().toString();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    byte[] content = "test".getBytes();
    when(file.getBytes()).thenReturn(content);

    String username = "user1";
    String maliciousName = "../outside/evil.jpg";

    // Act
    AttackResult result = base.execute(file, maliciousName, username);

    // Assert
    assertThat(result).isNotNull();

    File uploadDir = new File(baseDir, "/PathTraversal/" + username);
    assertThat(uploadDir).isDirectory();

    File[] uploadedFiles = uploadDir.listFiles();
    assertThat(uploadedFiles).isNotNull();
    assertThat(uploadedFiles.length).isEqualTo(1);

    File stored = uploadedFiles[0];
    // Ensure only the base name is used (no directory traversal)
    assertThat(stored.getName()).isEqualTo("evil.jpg");
    assertThat(stored.getCanonicalPath()).startsWith(uploadDir.getCanonicalPath());
    assertThat(Files.readAllBytes(stored.toPath())).isEqualTo(content);
  }

  @Test
  void execute_shouldAcceptSimpleFilenameUnchanged(@TempDir Path tmpDir) throws Exception {
    // Arrange
    String baseDir = tmpDir.toAbsolutePath().toString();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    byte[] content = "avatar".getBytes();
    when(file.getBytes()).thenReturn(content);

    String username = "user2";
    String fileName = "avatar.png";

    // Act
    AttackResult result = base.execute(file, fileName, username);

    // Assert
    assertThat(result).isNotNull();

    File uploadDir = new File(baseDir, "/PathTraversal/" + username);
    File expected = new File(uploadDir, fileName);
    assertThat(expected).exists();
    assertThat(FileCopyUtils.copyToByteArray(expected)).isEqualTo(content);
  }
}
