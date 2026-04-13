package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delta tests for ProfileUploadBase focusing on:
 * - Sanitization of fullName via FilenameUtils.getName().
 * - Ensuring uploaded files remain inside the intended user directory even
 *   when fullName contains path traversal sequences.
 */
class ProfileUploadBaseTest {

  @Test
  void execute_normalizesTraversalInFullNameAndWritesInsideUserDirectory()
      throws Exception {
    File tempRoot = Files.createTempDirectory("webgoat-home-").toFile();
    ProfileUploadBase base = new ProfileUploadBase(tempRoot.getAbsolutePath());

    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getBytes()).thenReturn("dummy".getBytes());

    String username = "alice";
    String traversalName = "../../etc/passwd";

    try {
      AttackResult result = base.execute(file, traversalName, username);

      File userDir = new File(tempRoot, "/PathTraversal/" + username);
      assertTrue(userDir.exists());

      File[] files = userDir.listFiles();
      assertTrue(files != null && files.length == 1);

      File stored = files[0];

      assertEquals("passwd", stored.getName());
      assertEquals(userDir.getCanonicalPath(), stored.getParentFile().getCanonicalPath());

      assertTrue(
          result.getStatus() == AttackResult.Status.INFO
              || result.getStatus() == AttackResult.Status.FAILURE
              || result.getStatus() == AttackResult.Status.SUCCESS);
    } finally {
      FileSystemUtils.deleteRecursively(tempRoot);
    }
  }
}
