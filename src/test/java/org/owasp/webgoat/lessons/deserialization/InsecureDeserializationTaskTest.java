package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/*
 * Delta test for:
 *   Source: src/main/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTask.java
 *   Test:   src/test/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTaskTest.java
 *
 * Focus: ObjectInputFilter introduction to restrict deserialization to allowed classes.
 */
public class InsecureDeserializationTaskTest {

  private final InsecureDeserializationTask task = new InsecureDeserializationTask();

  private String serializeForEndpoint(Object obj) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }
    String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    // Endpoint expects URL-safe token (reversed when handling request)
    return b64.replace('+', '-').replace('/', '_');
  }

  @Test
  void shouldAllowWhitelistedVulnerableTaskHolder() throws Exception {
    // Arrange: valid, whitelisted type
    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    String token = serializeForEndpoint(holder);

    // Act
    AttackResult result = task.completed(token);

    // Assert: filter must not immediately reject whitelisted type
    // We assert that it does NOT obviously fail due to invalid-version feedback.
    String feedback = result.getFeedback();
    boolean rejectedAsInvalidVersion =
        feedback != null && feedback.contains("insecure-deserialization.invalidversion");
    assertFalse(
        rejectedAsInvalidVersion,
        "Whitelisted VulnerableTaskHolder should not be rejected by ObjectInputFilter");
  }

  @Test
  void shouldNotSucceedForNonWhitelistedType() throws Exception {
    // Arrange: non-whitelisted type (e.g. Integer)
    Integer nonWhitelisted = 42;
    String token = serializeForEndpoint(nonWhitelisted);

    // Act
    AttackResult result = task.completed(token);

    // Assert: deserialization of non-whitelisted class must not yield success
    String feedback = result.getFeedback();
    boolean indicatesSuccess =
        feedback != null && feedback.toLowerCase().contains("success");
    assertFalse(
        indicatesSuccess,
        "Non-whitelisted type must not result in a successful AttackResult");
  }
}
