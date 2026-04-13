// batch_id: BATCH-003
// status: IN_PROGRESS
// test_file_path: src/test/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTaskTest.java
package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Delta tests for InsecureDeserializationTask focusing on the added
 * ObjectInputFilter that restricts deserialized types.
 */
public class InsecureDeserializationTaskTest {

  private String toWebGoatToken(Object object) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(object);
    }
    String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    return base64.replace('+', '-').replace('/', '_');
  }

  /** A serializable type not present in the ObjectInputFilter allowlist. */
  private static class DisallowedSerializable implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String value;

    DisallowedSerializable(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  @Test
  @DisplayName("completed rejects disallowed serializable type due to ObjectInputFilter")
  void completed_rejectsDisallowedType() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    String token = toWebGoatToken(new DisallowedSerializable("blocked"));

    var result = task.completed(token);

    // Filter should cause a generic invalidversion feedback for unknown classes.
    assertEquals(
        "insecure-deserialization.invalidversion",
        result.getFeedbackId(),
        "Disallowed type should be rejected by ObjectInputFilter");
  }

  @Test
  @DisplayName("completed still handles allowed java.lang.String as before")
  void completed_handlesAllowedStringType() throws Exception {
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    String token = toWebGoatToken("safe-string");

    var result = task.completed(token);

    // Existing behavior for String should remain unchanged.
    assertEquals(
        "insecure-deserialization.stringobject",
        result.getFeedbackId(),
        "String type should still be processed via existing failure path");
  }
}
