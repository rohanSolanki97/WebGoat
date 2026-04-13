package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for BlindSendFileAssignment focusing on path sanitization when constructing
 * user-specific directories for the secret file.
 *
 * Derived path: src/test/java/org/owasp/webgoat/lessons/xxe/BlindSendFileAssignmentTest.java
 */
public class BlindSendFileAssignmentTest {

  @Test
  void createSecretFileWithRandomContents_shouldSanitizeUsernameInDirectoryPath() throws Exception {
    // Arrange
    String baseDir = Files.createTempDirectory("webgoat-xxe-").toFile().getAbsolutePath();
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    Mockito.when(user.getUsername()).thenReturn("../evilUser");

    // Act
    // call initialize which in turn calls createSecretFileWithRandomContents(user)
    assignment.initialize(user);

    // Assert
    // The directory path must not contain traversal sequences and should end with sanitized username
    File expectedBase = new File(baseDir, "/XXE");
    File[] childDirs = expectedBase.listFiles(File::isDirectory);
    assertTrue(childDirs != null && childDirs.length == 1, "Exactly one user directory should be created");
    File userDir = childDirs[0];

    // Ensure directory name is the sanitized last path segment (no ../)
    assertEquals("evilUser", userDir.getName());
    // Ensure the directory is created under the intended base path
    assertTrue(userDir.getCanonicalPath().startsWith(expectedBase.getCanonicalPath()));
    // And that the secret.txt file exists within that sanitized directory
    File secretFile = new File(userDir, "secret.txt");
    assertTrue(secretFile.isFile());
  }

  @Test
  void initialize_shouldStoreFileContentsForUserInInternalMap() {
    // Arrange
    String baseDir = new File("target").getAbsolutePath();
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    Mockito.when(user.getUsername()).thenReturn("alice");

    // Act
    assignment.initialize(user);

    // Assert
    // Internal mapping from user to file contents should contain an entry for this user
    @SuppressWarnings("unchecked")
    Map<WebGoatUser, String> userToFileContents =
        (Map<WebGoatUser, String>)
            TestReflectionUtils.getFieldValue(assignment, "userToFileContents");
    assertTrue(userToFileContents.containsKey(user));
    String contents = userToFileContents.get(user);
    assertTrue(contents.startsWith("WebGoat 8.0 rocks..."), "Secret contents should match expected pattern");
  }

  /**
   * Minimal reflection helper to access private fields without modifying production code.
   */
  static class TestReflectionUtils {
    static Object getFieldValue(Object target, String fieldName) {
      try {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
