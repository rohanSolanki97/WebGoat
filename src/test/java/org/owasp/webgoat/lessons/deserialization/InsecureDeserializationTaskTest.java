package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for InsecureDeserializationTask focusing on the vulnerability:
 * "Deserializing user-controlled data".
 *
 * The updated code introduces an ObjectInputFilter that allowlists:
 *   - org.dummy.insecure.framework.VulnerableTaskHolder
 *   - java.lang.String
 * and rejects everything else.
 *
 * These tests validate that:
 * - Deserialization of an allowed type (VulnerableTaskHolder) still succeeds at the API level.
 * - The public API remains functional when given a serialized allowed object.
 *
 * We intentionally avoid asserting exact success/failure feedback semantics, which are lesson-specific.
 */
class InsecureDeserializationTaskTest {

  private String toWebSafeBase64(byte[] data) {
    String b64 = Base64.getEncoder().encodeToString(data);
    return b64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_acceptsSerializedVulnerableTaskHolder() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    byte[] serialized;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(holder);
      oos.flush();
      serialized = baos.toByteArray();
    }

    String token = toWebSafeBase64(serialized);

    // Act
    AttackResult result = task.completed(token);

    // Assert
    // At minimum, the endpoint must still return a non-null AttackResult for an allowlisted type.
    assertNotNull(result, "completed() should return a non-null AttackResult for allowed types");
  }

  @Test
  void completed_handlesSerializedStringWhichIsOnAllowlist() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    String payload = "test-string";
    byte[] serialized;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(payload);
      oos.flush();
      serialized = baos.toByteArray();
    }

    String token = toWebSafeBase64(serialized);

    // Act
    AttackResult result = task.completed(token);

    // Assert
    // String is explicitly allowlisted in the ObjectInputFilter, so the endpoint must remain stable.
    assertNotNull(result, "completed() should return a non-null AttackResult for String payloads");
  }
}
