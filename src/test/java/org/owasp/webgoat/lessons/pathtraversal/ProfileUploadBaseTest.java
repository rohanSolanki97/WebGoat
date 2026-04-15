// File: src/test/java/org/owasp/webgoat/lessons/pathtraversal/ProfileUploadBaseTest.java
package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

public class ProfileUploadBaseTest {

  @TempDir Path tempDir;

  @Test
  void execute_sanitizesFilenameToPreventPathTraversal() throws Exception {
    // Arrange
    String baseDir = tempDir.toString();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    Mockito.when(file.getBytes()).thenReturn("dummy".getBytes());

    String maliciousFullName = "../evil/../../escape.txt";
    String username = "user1";

    // Act
    AttackResult result = base.execute(file, maliciousFullName, username);

    // Assert
    // The uploaded file must be created under the sanitized user directory,
    // and traversal via '../' should not be honored.
    File userDir = new File(baseDir, "PathTraversal/" + username);
    File[] files = userDir.listFiles();
    assertTrue(files != null && files.length == 1, "One file should be stored for the user");
    File uploaded = files[0];

    assertTrue(
        uploaded.getCanonicalPath().startsWith(userDir.getCanonicalPath()),
        "Uploaded file must remain under the user's directory");
    assertTrue(!uploaded.getName().contains(".."), "Sanitized filename must not contain '..'");

    // Clean up explicitly in case tempDir handling differs
    FileSystemUtils.deleteRecursively(userDir);
    assertTrue(!userDir.exists());
  }

  @Test
  void cleanupAndCreateDirectoryForUser_sanitizesUsername() throws Exception {
    // Arrange
    String baseDir = tempDir.toString();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    String username = "../attacker";

    // Act
    File uploadDirectory = base.cleanupAndCreateDirectoryForUser(username);

    // Assert
    // Directory must be created under PathTraversal base and not outside via traversal
    File basePathTraversal = new File(baseDir, "PathTraversal");
    assertTrue(
        uploadDirectory.getCanonicalPath().startsWith(basePathTraversal.getCanonicalPath()),
        "User directory must remain under PathTraversal base directory");
  }
}
