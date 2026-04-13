package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Delta tests for ProfileUploadBase verifying that:
 * - The uploaded filename is sanitized with FilenameUtils.getName to prevent
 *   path traversal via fullName.
 * - The user-specific directory is also sanitized by username.
 */
public class ProfileUploadBaseTest {

  @Test
  @DisplayName("execute sanitizes fullName and prevents directory traversal in uploaded file path")
  void execute_sanitizesFullName() throws Exception {
    String baseDir = Files.createTempDirectory("webgoat-path-").toFile().getAbsolutePath();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    String username = "alice";
    String maliciousName = "../evil/escape.jpg";
    byte[] content = "dummy".getBytes();
    MockMultipartFile file =
        new MockMultipartFile("file", maliciousName, "image/jpeg", content);

    var result = base.execute(file, maliciousName, username);

    // The implementation should still consider this a normal profile update (no attemptWasMade),
    // and the actual file path must be confined under the sanitized directory.
    String userDirName = "alice";
    File expectedDir = new File(baseDir, "/PathTraversal/" + userDirName);
    assertTrue(
        expectedDir.exists() && expectedDir.isDirectory(),
        "User directory with sanitized username must exist");

    File[] files = expectedDir.listFiles();
    assertTrue(files != null && files.length == 1, "Exactly one file should be uploaded");

    // The stored file name must be the sanitized base name (no '../evil')
    assertEquals("escape.jpg", files[0].getName());
  }

  @Test
  @DisplayName("cleanupAndCreateDirectoryForUser sanitizes username for directory path")
  void cleanupAndCreateDirectoryForUser_sanitizesUsername() throws Exception {
    String baseDir = Files.createTempDirectory("webgoat-path-").toFile().getAbsolutePath();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    String maliciousUsername = "../otherUser";
    File dir = base.cleanupAndCreateDirectoryForUser(maliciousUsername);

    // Directory should be created under /PathTraversal/otherUser, not honoring '../'.
    File expected = new File(baseDir, "/PathTraversal/otherUser");
    assertEquals(
        expected.getCanonicalPath(),
        dir.getCanonicalPath(),
        "Username must be sanitized to prevent path traversal");
  }
}
