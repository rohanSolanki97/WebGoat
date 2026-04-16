package org.owasp.webgoat.lessons.xxe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for BlindSendFileAssignment focusing on the sanitized directory construction
 * introduced in createSecretFileWithRandomContents (path is no longer built directly from
 * user.getUsername()).
 */
public class BlindSendFileAssignmentTest {

  @Test
  void createSecretFileWithRandomContents_shouldSanitizeUsernameToPreventPathTraversal(
      @TempDir Path tmpDir) throws Exception {
    // Arrange
    String baseDir = tmpDir.toAbsolutePath().toString();
    CommentsCache commentsCache = mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = mock(WebGoatUser.class);
    // Username containing characters that could be used for path traversal
    when(user.getUsername()).thenReturn("../evil/..//user:name?*");

    // Act
    assignment.initialize(user); // triggers createSecretFileWithRandomContents(user)

    // Assert
    // Expect a directory directly under baseDir/XXE with sanitized name (no path separators)
    File xxeRoot = new File(baseDir, "/XXE");
    assertThat(xxeRoot).isDirectory();

    File[] children = xxeRoot.listFiles();
    assertThat(children).isNotNull();
    assertThat(children.length).isEqualTo(1);

    File userDir = children[0];
    // Ensure the directory name has only allowed characters as per the sanitization regex
    assertThat(userDir.getName()).matches("^[a-zA-Z0-9-_.]*$");
    // Ensure that 'evil' or '..' is not preserved as a directory level
    assertThat(userDir.getCanonicalPath()).doesNotContain("..").doesNotContain("evil");

    // And the secret file should exist inside the sanitized directory
    File secretFile = new File(userDir, "secret.txt");
    assertThat(secretFile).exists();
    assertThat(Files.readString(secretFile.toPath())).contains("WebGoat 8.0 rocks...");
  }
}
