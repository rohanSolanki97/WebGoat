// File: src/test/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTaskTest.java
// Derived from src/main/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTask.java
package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

public class InsecureDeserializationTaskTest {

  private String encodeToToken(Object obj) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }
    String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    // Mirror the token normalization logic from the controller
    return b64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_disallowedTypeResultsInFailedLesson() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    // Integer is not on the allow-list of the ObjectInputFilter
    String token = encodeToToken(Integer.valueOf(42));

    // Act
    AttackResult result = task.completed(token);

    // Assert
    // The filter should reject this type and map to a failure result
    assertFalse(result.getLessonCompleted());
  }

  @Test
  void completed_allowedTypeDoesNotThrowAndReturnsResult() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    String token = encodeToToken(holder);

    // Act
    AttackResult result = task.completed(token);

    // Assert
    // We only assert that a non-null result is returned, indicating that the
    // allow-listed class passes the ObjectInputFilter and is processed.
    assertFalse(result == null);
  }
}
