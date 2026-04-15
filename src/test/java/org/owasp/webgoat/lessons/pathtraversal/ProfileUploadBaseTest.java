package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

/**
 * Delta tests for ProfileUploadBase focusing on the changed behavior:
 * - The user-controlled fullName is sanitized with FilenameUtils.getName before file creation.
 */
public class ProfileUploadBaseTest {

  private File tempBaseDir;
  private ProfileUploadBase profileUploadBase;

  @BeforeEach
  void setUp() throws Exception {
    tempBaseDir = Files.createTempDirectory("webgoat-pathtraversal-test").toFile();
    profileUploadBase = new ProfileUploadBase(tempBaseDir.getAbsolutePath());
  }

  @AfterEach
  void tearDown() throws Exception {
    if (tempBaseDir != null && tempBaseDir.exists()) {
      FileSystemUtils.deleteRecursively(tempBaseDir);
    }
  }

  @Test
  void execute_sanitizesFullNameAndPreventsTraversal() throws Exception {
    String username = "bob";
    String maliciousFullName = "../evilDir/evil.txt";
    MockMultipartFile file =
        new MockMultipartFile("file", "evil.txt", "text/plain", "dummy".getBytes());

    profileUploadBase.execute(file, maliciousFullName, username);

    File userDir = new File(tempBaseDir, "/PathTraversal/" + username);
    File sanitizedFile = new File(userDir, "evil.txt");
    File rawTraversalFile = new File(userDir, maliciousFullName);

    assertTrue(
        sanitizedFile.exists(),
        "Sanitized filename should exist only within the user's PathTraversal directory");
    assertFalse(
        rawTraversalFile.exists(),
        "File path containing traversal sequences must not be created");
  }

  @Test
  void execute_createsFileForCleanName() throws Exception {
    String username = "alice";
    String cleanName = "profile.jpg";
    MockMultipartFile file =
        new MockMultipartFile("file", cleanName, "image/jpeg", "imageBytes".getBytes());

    profileUploadBase.execute(file, cleanName, username);

    File userDir = new File(tempBaseDir, "/PathTraversal/" + username);
    File expectedFile = new File(userDir, cleanName);

    assertTrue(
        expectedFile.exists(),
        "Clean filename should result in a file created in the expected user directory");
  }
}
