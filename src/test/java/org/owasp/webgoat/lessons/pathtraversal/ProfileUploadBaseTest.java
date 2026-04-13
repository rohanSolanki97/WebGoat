package org.owasp.webgoat.lessons.pathtraversal;

import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.CurrentUsername;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delta tests for ProfileUploadBase focusing on filename sanitization and path normalization
 * preventing path traversal via the uploaded filename.
 */
class ProfileUploadBaseTest {

  private static final String BASE_DIR = "build/tmp/profileUploadHome";

  @AfterEach
  void cleanup() throws Exception {
    Path base = Paths.get(BASE_DIR);
    if (Files.exists(base)) {
      FileSystemUtils.deleteRecursively(base);
    }
  }

  @Test
  void execute_shouldStoreFileInsideUserDirectoryWithSanitizedName() throws Exception {
    ProfileUploadBase base = new ProfileUploadBase(BASE_DIR);
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getBytes()).thenReturn("data".getBytes());

    String username = "bob";
    String originalName = "../outside.jpg";

    AttackResult result = base.execute(file, originalName, username);

    File userDir =
        new File(BASE_DIR + File.separator + "PathTraversal" + File.separator + username);
    File storedFile = userDir.listFiles() != null && userDir.listFiles().length > 0
        ? userDir.listFiles()[0]
        : null;

    org.junit.jupiter.api.Assertions.assertNotNull(storedFile);
    org.junit.jupiter.api.Assertions.assertTrue(storedFile.getName().endsWith("outside.jpg"));
    org.junit.jupiter.api.Assertions.assertTrue(
        storedFile.getCanonicalPath().startsWith(userDir.getCanonicalPath()));
  }

  @Test
  void execute_shouldRejectWhenFilenameResolvesOutsideUploadDirectory() throws Exception {
    ProfileUploadBase base = new ProfileUploadBase(BASE_DIR);
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getBytes()).thenReturn("data".getBytes());

    String username = "bob";
    // Even if attacker attempts traversal, sanitization + startsWith check should fail and
    // not report success
    AttackResult result = base.execute(file, "../../etc/passwd", username);

    org.junit.jupiter.api.Assertions.assertFalse(result.getLessonCompleted());
  }
}
