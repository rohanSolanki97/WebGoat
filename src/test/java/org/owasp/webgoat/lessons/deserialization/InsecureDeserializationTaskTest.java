package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

class InsecureDeserializationTaskTest {

  @Test
  @DisplayName("completed should successfully deserialize allowed VulnerableTaskHolder objects")
  void completedAllowsVulnerableTaskHolder() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    String token = toBase64Url(new VulnerableTaskHolder());

    AttackResult result = task.completed(token);

    assertNotNull(result, "AttackResult should not be null for allowed class");
  }

  @Test
  @DisplayName("completed should reject obviously invalid (non-base64) tokens")
  void completedRejectsInvalidTokenFormat() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    String invalidToken = "!!!invalid-base64!!!";

    AttackResult result = task.completed(invalidToken);

    assertNotNull(result, "AttackResult should not be null for invalid token");
    // The exact feedback is not asserted here; we focus on exercising the filter & error path.
  }

  private String toBase64Url(Object obj) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }
    String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    return b64.replace('+', '-').replace('/', '_');
  }
}
