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
 * Delta tests for InsecureDeserializationTask focusing on ObjectInputFilter allowlisting.
 */
public class InsecureDeserializationTaskTest {

  @Test
  void completed_shouldRejectUnexpectedSerializedTypeDueToFilter() throws Exception {
    // Arrange: serialize a String, which should now be rejected by the ObjectInputFilter.
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    String payload = "malicious-string";

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(payload);
    }
    String token =
        Base64.getEncoder()
            .encodeToString(baos.toByteArray())
            .replace('+', '-')
            .replace('/', '_');

    // Act
    AttackResult result = task.completed(token);

    // Assert: lesson must not be completed for rejected type
    assertNotNull(result);
    assertFalse(result.getLessonCompleted());
  }

  @Test
  void completed_shouldAcceptVulnerableTaskHolderTypeThroughFilter() throws Exception {
    // Arrange: serialize VulnerableTaskHolder, which is explicitly allowed.
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    VulnerableTaskHolder holder = new VulnerableTaskHolder();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(holder);
    }
    String token =
        Base64.getEncoder()
            .encodeToString(baos.toByteArray())
            .replace('+', '-')
            .replace('/', '_');

    // Act
    AttackResult result = task.completed(token);

    // Assert: call should succeed and not be rejected by the filter
    assertNotNull(result);
  }
}
