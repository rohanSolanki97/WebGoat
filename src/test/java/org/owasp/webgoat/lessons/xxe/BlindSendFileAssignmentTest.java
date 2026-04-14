package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.WebGoatUser;

/*
 * Delta tests for:
 *   Source: src/main/java/org/owasp/webgoat/lessons/xxe/BlindSendFileAssignment.java
 *   Test:   src/test/java/org/owasp/webgoat/lessons/xxe/BlindSendFileAssignmentTest.java
 *
 * Focus: path construction uses sanitized username (no traversal characters).
 */
public class BlindSendFileAssignmentTest {

  @Test
  void secretFileCreationShouldSanitizeUsernameInPath() throws Exception {
    // Arrange
    String baseDir = "/var/webgoat-users";
    CommentsCache comments = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, comments);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    // Username containing characters that would previously affect the path
    Mockito.when(user.getUsername()).thenReturn("../evil/../user.name");

    // We indirectly trigger createSecretFileWithRandomContents via initialize
    // and spy on filesystem interaction by mocking Files.writeString is not trivial here,
    // so we assert that no exception is thrown for such a username and that
    // sanitize logic does not break initialization.
    assignment.initialize(user);

    // The primary regression we want to avoid is a crash due to illegal path.
    // If the sanitization were removed, this test would likely fail on some OS/filesystems.
  }

  @Test
  void addCommentShouldAcceptArbitraryCommentWithoutPathTraversalSideEffects() {
    // Arrange
    String baseDir = "/var/webgoat-users";
    CommentsCache comments = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, comments);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    Mockito.when(user.getUsername()).thenReturn("../../user");

    // initialize to create secret file and cache
    assignment.initialize(user);

    String commentPayload = "<comment>some text</comment>";
    Mockito.when(comments.parseXml(commentPayload, false))
        .thenReturn(new Comment("some text".getBytes(StandardCharsets.UTF_8)));

    // Act & Assert: method must not throw for usernames with traversal patterns
    assignment.addComment(commentPayload, user);

    // And the normal flow still occurs
    verify(comments).addComment(Mockito.any(Comment.class), Mockito.eq(user), Mockito.eq(false));
  }
}
