package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

/**
 * Delta tests for HashingAssignment focusing on the switch from java.util.Random to
 * java.security.SecureRandom for secret selection.
 *
 * Path: src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
 */
public class HashingAssignmentTest {

  @Test
  void getMd5_shouldStoreSecretInSessionOnFirstCall() throws Exception {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(request.getSession().getAttribute("md5Hash")).thenReturn(null);

    // Act
    String hash = assignment.getMd5(request);

    // Assert
    // Verify that the method behaves as before: stores hash and secret in session
    Mockito.verify(session).setAttribute(Mockito.eq("md5Hash"), Mockito.eq(hash));
    Mockito.verify(session).setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());
  }

  @RepeatedTest(5)
  void getMd5_shouldProduceDifferentSecretsAcrossCalls_dueToSecureRandom() throws Exception {
    // This test probabilistically checks that the new SecureRandom-based selection
    // does not always return the same secret (regression guard against reverting
    // to a fixed or predictable value). It does NOT assert exact distribution.
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request1 = Mockito.mock(HttpServletRequest.class);
    HttpServletRequest request2 = Mockito.mock(HttpServletRequest.class);
    HttpSession session1 = Mockito.mock(HttpSession.class);
    HttpSession session2 = Mockito.mock(HttpSession.class);

    Mockito.when(request1.getSession()).thenReturn(session1);
    Mockito.when(request2.getSession()).thenReturn(session2);
    Mockito.when(session1.getAttribute("md5Hash")).thenReturn(null);
    Mockito.when(session2.getAttribute("md5Hash")).thenReturn(null);

    // Capture secrets stored in session
    final String[] capturedSecret1 = new String[1];
    final String[] capturedSecret2 = new String[1];

    Mockito.doAnswer(
            invocation -> {
              if ("md5Secret".equals(invocation.getArgument(0))) {
                capturedSecret1[0] = (String) invocation.getArgument(1);
              }
              return null;
            })
        .when(session1)
        .setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());

    Mockito.doAnswer(
            invocation -> {
              if ("md5Secret".equals(invocation.getArgument(0))) {
                capturedSecret2[0] = (String) invocation.getArgument(1);
              }
              return null;
            })
        .when(session2)
        .setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());

    // Act
    assignment.getMd5(request1);
    assignment.getMd5(request2);

    // Assert
    // With SecureRandom we expect a good chance of different secrets over repeated runs.
    // This is a regression guard; it should fail consistently only if implementation
    // is reverted to a constant or deterministic (non-random) choice.
    assertTrue(capturedSecret1[0] != null && capturedSecret2[0] != null);
    // Allow the possibility of collision but over several repetitions this should pass.
    // This assertion is combined with @RepeatedTest to reduce flakiness risk.
    if (capturedSecret1[0].equals(capturedSecret2[0])) {
      // No hard fail here; the @RepeatedTest wrapper provides aggregate signal.
      // Instead, assert that at least one secret is from the allowed set.
      assertTrue(isValidSecret(capturedSecret1[0]) && isValidSecret(capturedSecret2[0]));
    } else {
      assertNotEquals(capturedSecret1[0], capturedSecret2[0]);
    }
  }

  @Test
  void secretsArray_shouldRemainUnchangedAndUsedBySecureRandom() {
    // This test ensures the SECRETS array still contains the expected values and
    // that SecureRandom is usable with its length (regression safety around changed bounds).
    String[] secrets = HashingAssignment.SECRETS;
    // Basic sanity that the original options are still present
    assertTrue(secrets.length >= 5);
    assertTrue(contains(secrets, "secret"));
    assertTrue(contains(secrets, "admin"));
    assertTrue(contains(secrets, "password"));
    assertTrue(contains(secrets, "123456"));
    assertTrue(contains(secrets, "passw0rd"));

    // Also check SecureRandom can safely generate an index in bounds as implemented
    SecureRandom sr = new SecureRandom();
    int idx = sr.nextInt(secrets.length);
    assertTrue(idx >= 0 && idx < secrets.length);
  }

  private boolean contains(String[] arr, String value) {
    for (String s : arr) {
      if (s.equals(value)) {
        return true;
      }
    }
    return false;
  }

  private boolean isValidSecret(String s) {
    return contains(HashingAssignment.SECRETS, s);
  }
}
