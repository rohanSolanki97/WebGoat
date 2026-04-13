package org.owasp.webgoat.lessons.deserialization;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for InsecureDeserializationTask focusing on the ObjectInputFilter whitelist that
 * restricts allowed deserialization types.
 */
class InsecureDeserializationTaskTest {

  private String encodeObjectToToken(Object obj) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }
    String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    // reverse replacement done in the controller
    return b64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_shouldFailForDisallowedDeserializationType() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    // Some arbitrary Serializable type that is not whitelisted by the ObjectInputFilter
    java.io.Serializable malicious =
        new java.io.Serializable() {
          private static final long serialVersionUID = 1L;
        };
    String token = encodeObjectToToken(malicious);

    // Act
    AttackResult result = task.completed(token);

    // Assert: lesson remains incomplete because the type is rejected by the filter
    org.junit.jupiter.api.Assertions.assertFalse(result.getLessonCompleted());
  }

  @Test
  void completed_shouldHandleInvalidBase64InputSafely() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    String invalidToken = "not-base64!!";

    // Act
    AttackResult result = task.completed(invalidToken);

    // Assert
    org.junit.jupiter.api.Assertions.assertFalse(result.getLessonCompleted());
  }
}
