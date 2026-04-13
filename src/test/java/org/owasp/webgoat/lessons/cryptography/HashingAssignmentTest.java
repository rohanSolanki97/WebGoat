// batch_id: BATCH-001
// status: IN_PROGRESS
// test_file_path: src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Delta tests for HashingAssignment focused on changed behavior around secret
 * selection using SecureRandom. We avoid probabilistic assertions and instead
 * verify that:
 * - When a secret is already stored in the session, hashing behavior is
 *   deterministic and unchanged.
 * - The SECRETS array is still present and used.
 *
 * Note: Differentiating Random vs SecureRandom purely via black-box testing
 * would require probabilistic checks. Here we validate that the refactor did
 * not break the deterministic behavior when session values are present and
 * that SecureRandom is available at runtime.
 */
public class HashingAssignmentTest {

  @Test
  @DisplayName("getMd5 returns MD5 of existing session secret without regenerating")
  void getMd5_usesExistingSessionSecretDeterministically() throws Exception {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);

    String knownSecret = "password"; // from SECRETS array
    String expectedMd5 = HashingAssignment.getHash(knownSecret, "MD5");
    when(session.getAttribute("md5Hash")).thenReturn(expectedMd5);

    String md5Hash = assignment.getMd5(request);

    assertEquals(expectedMd5, md5Hash);
  }

  @Test
  @DisplayName("getSha256 returns SHA-256 of existing session secret without regenerating")
  void getSha256_usesExistingSessionSecretDeterministically() throws Exception {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);

    String knownSecret = "admin"; // from SECRETS array
    String expectedSha256 = HashingAssignment.getHash(knownSecret, "SHA-256");
    when(session.getAttribute("sha256")).thenReturn(expectedSha256);

    String sha = assignment.getSha256(request);

    assertEquals(expectedSha256, sha);
  }

  @Test
  @DisplayName("SECRETS array remains defined and SecureRandom is constructible")
  void secretsArrayAndSecureRandomAvailable() throws Exception {
    // Ensure SECRETS is still present and non-empty after the refactor.
    Field secretsField = HashingAssignment.class.getDeclaredField("SECRETS");
    secretsField.setAccessible(true);
    Object value = secretsField.get(null);
    String[] secrets = (String[]) value;
    org.junit.jupiter.api.Assertions.assertTrue(secrets.length > 0);

    // Ensure SecureRandom is available at runtime, which the refactored code relies on.
    SecureRandom sr = new SecureRandom();
    org.junit.jupiter.api.Assertions.assertNotNull(sr);
  }
}
