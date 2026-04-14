package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for InsecureDeserializationTask focusing on the added ObjectInputFilter:
 * - Confirms that allowed class (VulnerableTaskHolder) deserializes successfully.
 * - Confirms that disallowed class causes failure, demonstrating the filter blocks
 *   previously possible gadget deserialization.
 */
public class InsecureDeserializationTaskTest {

  private String toWebSafeBase64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes).replace('+', '-').replace('/', '_');
  }

  @Test
  void allowsDeserializationOfVulnerableTaskHolder() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    holder.setDelay(1); // minimal delay to exercise logic

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(holder);
    }
    String token = toWebSafeBase64(baos.toByteArray());

    // Act
    AttackResult result = task.completed(token);

    // Assert: deserialization of the allowed class should succeed (no invalid-class error)
    assertNotNull(result, "Result should not be null for allowed class");
  }

  @Test
  void blocksDeserializationOfDisallowedClass() throws Exception {
    // Arrange: serialize a String, which is not VulnerableTaskHolder.
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject("some-string-object");
    }
    String token = toWebSafeBase64(baos.toByteArray());

    // Act
    AttackResult result = task.completed(token);

    // Assert: non-VulnerableTaskHolder should not complete the lesson successfully
    assertFalse(
        result.getLessonCompleted(),
        "Deserialization of disallowed types should not complete the lesson");
  }
}
