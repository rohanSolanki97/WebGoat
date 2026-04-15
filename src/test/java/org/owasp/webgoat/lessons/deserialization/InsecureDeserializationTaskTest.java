package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Delta tests for InsecureDeserializationTask focusing on the changed behavior:
 * - Application of ObjectInputFilter to restrict deserialization to an allowlist of classes.
 *
 * These tests verify:
 * - Deserialization of an allowed type (VulnerableTaskHolder) still succeeds.
 * - Deserialization of a disallowed type triggers the filter and results in a failure response.
 */
public class InsecureDeserializationTaskTest {

  private InsecureDeserializationTask task;

  @BeforeEach
  void setUp() {
    task = new InsecureDeserializationTask();
  }

  private String serializeToBase64UrlSafe(Object o) throws Exception {
    var baos = new java.io.ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
    }
    String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    // reverse the replacement done in the controller ( '-' -> '+', '_' -> '/' )
    return base64.replace('+', '-').replace('/', '_');
  }

  @Test
  void completed_allowsDeserializationOfWhitelistedType() throws Exception {
    // Arrange: create a token containing a whitelisted type
    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    String token = serializeToBase64UrlSafe(holder);

    // Act
    var result = task.completed(token);

    // Assert: success or failure based only on timing logic, but not on class type rejection
    // We can't easily control the artificial delay here, so just assert that filter did not
    // reject the class (i.e., we didn't get the 'wrongobject' or 'stringobject' feedback).
    String feedback = result.getFeedback();
    boolean notTypeError =
        feedback == null
            || (!feedback.contains("insecure-deserialization.wrongobject")
                && !feedback.contains("insecure-deserialization.stringobject"));
    assertTrue(
        notTypeError,
        "Whitelisted type should not be rejected by ObjectInputFilter, "
            + "even if timing rules still cause failure.");
  }

  @Test
  void completed_rejectsNonWhitelistedType() throws Exception {
    // Arrange: serialize a clearly non-whitelisted type (e.g. this test class itself)
    Object malicious = new Object();
    String token = serializeToBase64UrlSafe(malicious);

    // Act
    var result = task.completed(token);

    // Assert: result should indicate an invalid version / rejection due to filter or class issue
    String feedback = result.getFeedback();
    // The exact message depends on how the exception is mapped, but we expect one of the failure
    // feedback keys, not success.
    boolean isFailureFeedback =
        feedback != null
            && (feedback.contains("insecure-deserialization.invalidversion")
                || feedback.contains("insecure-deserialization.wrongobject")
                || feedback.contains("insecure-deserialization.stringobject"));
    assertTrue(
        isFailureFeedback,
        "Non-whitelisted type should be rejected by the filter or cause a failure feedback.");
  }
}
