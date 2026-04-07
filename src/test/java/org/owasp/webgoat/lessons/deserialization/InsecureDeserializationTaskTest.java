package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests focusing on the added ObjectInputFilter which restricts deserialization
 * to an allow-list of classes.
 */
public class InsecureDeserializationTaskTest {

  private String toToken(Object o) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
    }
    String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    // mirror token.replace('-', '+').replace('_', '/')
    return b64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_rejectsDeserializationOfDisallowedClass() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    // Use a commonly available type that is not on the allow-list
    String token = toToken(Integer.valueOf(42));

    // Act
    AttackResult result = task.completed(token);

    // Assert
    // Core secure behavior: payloads of disallowed types must not lead to a successful result.
    assertFalse(result.isLessonSolved());
  }
}
