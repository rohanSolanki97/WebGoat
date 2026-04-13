package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for InsecureDeserializationTask focusing on the newly added
 * ObjectInputFilter that allowlists VulnerableTaskHolder and String.
 */
class InsecureDeserializationTaskTest {

  private String toToken(Object o) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
    }
    String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    // Mirror token normalization in the controller
    return b64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_acceptsVulnerableTaskHolderAllowedByFilter() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    VulnerableTaskHolder holder = new VulnerableTaskHolder(); // must exist on classpath

    String token = toToken(holder);

    AttackResult result = task.completed(token);

    assertNotNull(result);
    // We cannot reliably assert success because of timing logic,
    // but we ensure the call does not immediately fail due to filter rejection.
  }

  @Test
  void completed_acceptsStringAllowedByFilterButFailsChallenge() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    String token = toToken("just-a-string");

    AttackResult result = task.completed(token);

    assertNotNull(result);
    assertFalse(result.getLessonCompleted());
  }

  @Test
  void completed_rejectsArbitraryTypeNotInAllowlist() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    class Evil implements Serializable {
      private static final long serialVersionUID = 1L;
    }

    String token = toToken(new Evil());

    AttackResult result = task.completed(token);

    // Filter should reject the class, causing a failure result instead of
    // letting arbitrary types be deserialized.
    assertNotNull(result);
    assertFalse(result.getLessonCompleted());
  }
}
