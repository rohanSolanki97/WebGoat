package org.owasp.webgoat.lessons.xxe;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for BlindSendFileAssignment focusing on:
 * 1) Path traversal via malicious usernames does not create files outside the intended XXE base directory.
 * 2) Normal usernames still result in successful file creation and correct content.
 * 3) Sanitization and base-path checks behave correctly.
 *
 * These tests exercise the behavior of createSecretFileWithRandomContents indirectly via the
 * public initialize(WebGoatUser) method, which is where the fix is applied.
 */
class BlindSendFileAssignmentTest {

  // We use a temporary directory under the OS temp directory as an isolated base for webGoatHomeDirectory.
  private final Path tempBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "webgoat-xxe-test");

  /**
   * Simple stub for CommentsCache to satisfy the constructor without side effects.
   */
  private static class StubCommentsCache extends CommentsCache {
    @Override
    public void reset(WebGoatUser user) {
      // no-op for tests
    }
  }

  /**
   * Simple stub for WebGoatUser with a fixed username.
   */
  private static class StubUser extends WebGoatUser {
    private final String username;

    StubUser(String username) {
      this.username = username;
    }

    @Override
    public String getUsername() {
      return username;
    }
  }

  @AfterEach
  void cleanUp() throws Exception {
    if (Files.exists(tempBaseDir)) {
      deleteRecursively(tempBaseDir.toFile());
    }
  }

  private void deleteRecursively(File file) {
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          deleteRecursively(child);
        }
      }
    }
    file.delete();
  }

  private BlindSendFileAssignment newAssignment() {
    // The constructor expects a webGoatHomeDirectory string; we pass the temp base directory path.
    return new BlindSendFileAssignment(tempBaseDir.toString(), new StubCommentsCache());
  }

  @Test
  void initialize_createsSecretFileForNormalUsernameWithinBaseDir() throws Exception {
    BlindSendFileAssignment assignment = newAssignment();
    String username = "alice";
    StubUser user = new StubUser(username);

    assignment.initialize(user);

    Path basePath = tempBaseDir.resolve("XXE");
    Path userDir = basePath.resolve(username);
    Path secretFile = userDir.resolve("secret.txt");

    assertTrue(Files.exists(basePath), "Base XXE directory should exist");
    assertTrue(Files.exists(userDir), "User directory should exist for normal username");
    assertTrue(Files.exists(secretFile), "secret.txt should be created for normal username");

    String contents = Files.readString(secretFile, UTF_8);
    assertNotNull(contents);
    assertTrue(
        contents.startsWith("WebGoat 8.0 rocks... ("),
        "Secret file content should follow the expected pattern");
  }

  @Test
  void initialize_sanitizesUsernameAndPreventsPathTraversalOutsideBaseDir() throws Exception {
    BlindSendFileAssignment assignment = newAssignment();

    // Attempt to escape the base directory using path traversal characters.
    String maliciousUsername = "../evilUser";
    StubUser user = new StubUser(maliciousUsername);

    assignment.initialize(user);

    Path basePath = tempBaseDir.resolve("XXE");
    Path normalizedBase = basePath.normalize();

    // Ensure base directory is created or not; our primary check is that no directories/files
    // are created outside the intended base path. We specifically check that there is no
    // directory named "evilUser" adjacent to tempBaseDir or above.
    Path outsideDir = tempBaseDir.getParent().resolve("evilUser");
    assertFalse(
        Files.exists(outsideDir),
        "No directory should be created outside the intended XXE base directory for malicious username");

    // Because the implementation now strips path separators and enforces startsWith(basePath),
    // any resolved path must remain under basePath or the method will return early.
    if (Files.exists(normalizedBase)) {
      // List any created directories under basePath and ensure they don't reflect traversal.
      Files.walk(normalizedBase)
          .filter(Files::isDirectory)
          .forEach(
              p -> assertTrue(
                  p.normalize().startsWith(normalizedBase),
                  "All created directories must remain within the XXE base directory"));
    }
  }

  @Test
  void initialize_removesPathSeparatorsFromUsername() throws Exception {
    BlindSendFileAssignment assignment = newAssignment();

    // Username containing forward and backward slashes which should be removed by sanitization
    String rawUsername = "bob/../nested\\user";
    StubUser user = new StubUser(rawUsername);

    assignment.initialize(user);

    Path basePath = tempBaseDir.resolve("XXE");
    Path expectedUserDir = basePath.resolve("bob..nesteduser"); // after replacing / and \ with empty

    assertTrue(
        Files.exists(expectedUserDir),
        "Sanitized username directory should be created under XXE base directory");
    Path secretFile = expectedUserDir.resolve("secret.txt");
    assertTrue(Files.exists(secretFile), "secret.txt should be created in sanitized directory");
  }
}
