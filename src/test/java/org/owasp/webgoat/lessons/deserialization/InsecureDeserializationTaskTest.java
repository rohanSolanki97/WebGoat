package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * File path (derived from main source): src/test/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTaskTest.java
 *
 * Delta tests for InsecureDeserializationTask verifying that the ObjectInputFilter allowlist:
 * - allows VulnerableTaskHolder (the intended type for the lesson),
 * - blocks other types such as java.lang.Integer.
 */
class InsecureDeserializationTaskTest {

  private String toUrlSafeBase64(byte[] bytes) {
    String b64 = Base64.getEncoder().encodeToString(bytes);
    return b64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_acceptsAllowedClassVulnerableTaskHolder() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(holder);
    }
    String token = toUrlSafeBase64(bos.toByteArray());

    AttackResult result = task.completed(token);

    // Exact feedback text is lesson-specific; the key property for the delta is that
    // this allowed type does not get rejected by the new ObjectInputFilter.
    assertFalse(
        result.getFeedback().contains("invalidversion"),
        "Allowed class should not be blocked by ObjectInputFilter");
  }

  @Test
  void completed_rejectsDisallowedClass() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    Integer disallowed = Integer.valueOf(42);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(disallowed);
    }
    String token = toUrlSafeBase64(bos.toByteArray());

    AttackResult result = task.completed(token);

    assertFalse(result.getLessonCompleted(), "Disallowed class must not pass validation");
  }
}
