// File: src/test/java/org/owasp/webgoat/lessons/xxe/BlindSendFileAssignmentTest.java
package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.container.users.WebGoatUser;
import org.springframework.beans.factory.annotation.Value;

public class BlindSendFileAssignmentTest {

  @TempDir Path tempDir;

  @Test
  void createSecretFileWithRandomContents_sanitizesUsernameInPath() throws Exception {
    // Arrange
    String baseDir = tempDir.toString();
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    // The constructor is annotated with @Value but at unit-test level we just pass the value
    BlindSendFileAssignment assignment =
        new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    // Attempt directory traversal in username
    when(user.getUsername()).thenReturn("../evilUser");

    // Act
    assignment.initialize(user);

    // Assert
    // The sanitized directory must be under the configured base directory and must not traverse
    // outside via '../'.
    File expectedBase = new File(baseDir, "XXE");
    File[] userDirs = expectedBase.listFiles();
    assertTrue(userDirs != null && userDirs.length == 1, "Exactly one user directory expected");

    File createdDir = userDirs[0];
    assertTrue(
        createdDir.getCanonicalPath().startsWith(expectedBase.getCanonicalPath()),
        "Created directory must remain under the XXE base directory");

    // Ensure no directory named ".." or "evilUser" above base exists
    Path forbiddenPath = tempDir.resolve("..").resolve("evilUser").normalize();
    assertTrue(Files.notExists(forbiddenPath), "Traversal target must not be created");
  }

  @Test
  void addComment_returnsFailedWhenSolutionNotContained() {
    // Arrange
    String baseDir = tempDir.toString();
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment =
        new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    when(user.getUsername()).thenReturn("user");
    assignment.initialize(user);

    String comment = "some harmless comment";

    // Act
    AttackResult result = assignment.addComment(comment, user);

    // Assert
    // Behavior outside of path fix remains: unsuccessful attempt should not complete lesson
    assertTrue(!result.getLessonCompleted());
  }
}
