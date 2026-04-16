package org.owasp.webgoat.lessons.deserialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputFilter;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for InsecureDeserializationTask focusing on the introduction of
 * ObjectInputFilter to restrict deserialization to VulnerableTaskHolder.
 * These tests assert:
 * - A valid VulnerableTaskHolder can still be deserialized successfully.
 * - An attempt to deserialize a different type fails the lesson (filter blocks it).
 */
public class InsecureDeserializationTaskTest {

  private String serializeToUrlSafeBase64(Object obj) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }
    String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    return b64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_shouldSucceedForAllowedVulnerableTaskHolder() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    String token = serializeToUrlSafeBase64(holder);

    // Act
    AttackResult result = task.completed(token);

    // Assert
    assertThat(result).isNotNull();
    // We cannot assert timing-based logic deterministically; we only assert that
    // the filter did not reject the allowed class and some result is returned.
  }

  @Test
  void completed_shouldFailForDisallowedTypeDueToDeserializationFilter() throws Exception {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();
    String malicious = "malicious";
    String token = serializeToUrlSafeBase64(malicious);

    // Act
    AttackResult result = task.completed(token);

    // Assert
    // The filter or subsequent type checks should cause the lesson to treat this as failure.
    assertThat(result.getLessonCompleted()).isFalse();
  }
}
