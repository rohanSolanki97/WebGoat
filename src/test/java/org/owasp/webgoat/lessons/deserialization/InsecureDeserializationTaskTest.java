package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for InsecureDeserializationTask focusing on the ObjectInputFilter allowlist.
 */
class InsecureDeserializationTaskTest {

  private String toUrlSafeBase64(Serializable obj) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
    }
    String base64 = Base64.getEncoder().encodeToString(bos.toByteArray());
    return base64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_allowsVulnerableTaskHolder() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    VulnerableTaskHolder holder = new VulnerableTaskHolder();

    String token = toUrlSafeBase64(holder);

    AttackResult result = task.completed(token);

    // If the allowlist is correctly configured to include VulnerableTaskHolder,
    // the call should complete successfully and set a SUCCESS status.
    assertEquals(AttackResult.Status.SUCCESS, result.getStatus());
  }

  @Test
  void completed_blocksNonWhitelistedSerializableClass() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    Serializable nonWhitelisted =
        new Serializable() {
          private static final long serialVersionUID = 1L;
        };

    String token = toUrlSafeBase64(nonWhitelisted);

    AttackResult result = task.completed(token);

    // The ObjectInputFilter should reject this class, and the endpoint maps
    // such failures to a FAILED status.
    assertEquals(AttackResult.Status.FAILURE, result.getStatus());
  }
}
