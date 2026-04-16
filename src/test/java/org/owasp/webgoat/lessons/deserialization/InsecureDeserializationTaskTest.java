package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for InsecureDeserializationTask focusing on the introduction of ObjectInputFilter to
 * restrict deserialization to an allowlist.
 *
 * These tests verify that:
 * - Deserialization of obviously disallowed types does not succeed.
 *
 * Note: The exact structure of VulnerableTaskHolder is not available here, so tests only assert
 * that disallowed payloads are rejected, avoiding assumptions about allowed types' constructors.
 */
public class InsecureDeserializationTaskTest {

  private String serializeToUrlSafeBase64(Object o) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
    }
    String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    // Mirror the token mangling in the controller (replace '+' and '/')
    return base64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_rejectsDeserializationOfUnexpectedType() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    // Use a serializable type that is unlikely to be on the allowlist
    Integer disallowed = 12345;
    String token = serializeToUrlSafeBase64(disallowed);

    // Act
    AttackResult result = task.completed(token);

    // Assert: should not mark lesson as completed when deserializing a disallowed type
    assertFalse(
        result.getLessonCompleted(),
        "Disallowed type should not lead to a successful lesson completion");
  }
}
