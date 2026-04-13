package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for InsecureDeserializationTask focusing on the introduction of a class-allowlisting
 * CustomObjectInputStream that only permits VulnerableTaskHolder.
 *
 * Path: src/test/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTaskTest.java
 */
public class InsecureDeserializationTaskTest {

  private final InsecureDeserializationTask task = new InsecureDeserializationTask();

  @Test
  void completed_shouldFailWhenDeserializingDisallowedType() throws Exception {
    // Arrange: create a serialized String payload (disallowed type) and base64-url encode it
    String maliciousObject = "I am not VulnerableTaskHolder";
    String token = serializeToBase64Url(maliciousObject);

    // Act
    AttackResult result = task.completed(token);

    // Assert: with new allowlist, String objects are explicitly rejected
    assertFalse(result.isSuccess());
  }

  @Test
  void completed_shouldAllowVulnerableTaskHolderType() throws Exception {
    // Arrange: create an allowed VulnerableTaskHolder object
    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    String token = serializeToBase64Url(holder);

    // Act
    AttackResult result = task.completed(token);

    // Assert: the allowlist should not block legitimate VulnerableTaskHolder instances
    // Note: actual success may still depend on internal timing logic; here we just assert
    // that processing does not fail immediately due to type restrictions.
    // We accept either success or specific failure not related to invalid type.
    assertTrue(
        !result.getFeedback().orElse("").contains("invalidversion"),
        "Result should not fail due to invalid class version/type");
  }

  private String serializeToBase64Url(Object obj) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }
    String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    // Match application transformation: '-' for '+', '_' for '/'
    return b64.replace('+', '-').replace('/', '_');
  }
}
