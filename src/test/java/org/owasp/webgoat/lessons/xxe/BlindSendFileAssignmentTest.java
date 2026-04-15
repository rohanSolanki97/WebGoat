package org.owasp.webgoat.lessons.xxe;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta test for BlindSendFileAssignment (BATCH-006)
 * Path: src/test/java/org/owasp/webgoat/lessons/xxe/BlindSendFileAssignmentTest.java
 *
 * Focus: directory path for secret file creation now uses a sanitized username,
 * removing traversal characters.
 */
class BlindSendFileAssignmentTest {

  @Test
  void initialize_shouldCreateDirectoryUsingSanitizedUsername() throws Exception {
    // Arrange
    File tmp = Files.createTempDirectory("webgoat-home-xxe-").toFile();
    String baseDir = tmp.getAbsolutePath();
    CommentsCache comments = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, comments);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    // Username with characters that must be stripped by the sanitization regex
    String rawUsername = "../evil/user..//name";
    Mockito.when(user.getUsername()).thenReturn(rawUsername);

    // Act
    assignment.initialize(user);

    // Assert
    // As per fix: sanitizedUsername = user.getUsername().replaceAll("[^a-zA-Z0-9-_.]", "");
    String expectedSanitized = rawUsername.replaceAll("[^a-zA-Z0-9-_.]", "");
    File expectedDir = new File(baseDir, "/XXE/" + expectedSanitized);
    File secretFile = new File(expectedDir, "secret.txt");

    assertThat(expectedDir.isDirectory()).isTrue();
    assertThat(secretFile.isFile()).isTrue();

    // Ensure raw username path is not used
    File rawDir = new File(baseDir, "/XXE/" + rawUsername);
    assertThat(rawDir.getCanonicalPath()).isNotEqualTo(expectedDir.getCanonicalPath());
  }
}
