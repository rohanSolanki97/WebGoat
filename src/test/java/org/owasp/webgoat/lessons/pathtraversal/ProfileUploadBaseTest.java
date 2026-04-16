package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delta tests for ProfileUploadBase focusing on secure path handling:
 * - username and fullName are sanitized before being used in paths.
 * - uploaded files are constrained to the intended user directory.
 */
public class ProfileUploadBaseTest {

  private Path tempHome;

  private ProfileUploadBase createBase() throws Exception {
    tempHome = Files.createTempDirectory("webgoat-profile-home-");
    return new ProfileUploadBase(tempHome.toString());
  }

  @AfterEach
  void cleanup() throws Exception {
    if (tempHome != null) {
      Files.walk(tempHome)
          .sorted((a, b) -> b.compareTo(a))
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  @Test
  void execute_withNormalInputs_savesFileWithinUserDirectory() throws Exception {
    // Arrange
    ProfileUploadBase base = createBase();
    MultipartFile file =
        new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "dummy".getBytes());
    String username = "bob";
    String fullName = "avatar.jpg";

    // Act
    base.execute(file, fullName, username);

    // Assert
    File userDir = tempHome.resolve("PathTraversal").resolve(username).toFile();
    File uploaded = new File(userDir, fullName);
    assertTrue(uploaded.exists(), "File should be stored within the per-user directory");
  }

  @Test
  void execute_withPathTraversalInFilename_doesNotEscapeUserDirectory() throws Exception {
    // Arrange
    ProfileUploadBase base = createBase();
    MultipartFile file =
        new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "dummy".getBytes());
    String username = "alice";
    String maliciousFullName = "../evil.txt";

    // Act
    base.execute(file, maliciousFullName, username);

    // Assert: sanitized filename should drop traversal and only use base name 'evil.txt'
    File expectedDir = tempHome.resolve("PathTraversal").resolve("alice").toFile();
    File expectedFile = new File(expectedDir, "evil.txt");
    assertTrue(
        expectedFile.exists(),
        "Sanitized filename should be used; file must remain inside the intended directory");

    // confirm no file was created above the base directory
    File parent = tempHome.getParent().toFile();
    File unexpected = new File(parent, "evil.txt");
    assertFalse(
        unexpected.exists(),
        "Path traversal in filename must not create files outside the home directory");
  }

  @Test
  void cleanupAndCreateDirectoryForUser_withTraversalUsername_staysInsideBaseDirectory()
      throws Exception {
    // Arrange
    ProfileUploadBase base = createBase();
    String maliciousUsername = "../attacker";

    // Act
    File uploadDir = base.cleanupAndCreateDirectoryForUser(maliciousUsername);

    // Assert
    // Directory name should be sanitized to 'attacker'
    File expectedDir = tempHome.resolve("PathTraversal").resolve("attacker").toFile();
    assertTrue(
        expectedDir.exists(),
        "Sanitized username directory should exist under the configured home directory");

    // verify returned directory is inside base
    assertTrue(
        uploadDir.toPath().normalize().startsWith(tempHome.toRealPath()),
        "User directory must not escape the configured home directory");
  }
}
