package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HashingAssignmentTest {

  @Test
  @DisplayName("getMd5 should store secret and hash in session on first call")
  void getMd5StoresValuesInSession() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);

    assignment.getMd5(request);

    Mockito.verify(session).setAttribute(Mockito.eq("md5Hash"), Mockito.any(String.class));
    Mockito.verify(session).setAttribute(Mockito.eq("md5Secret"), Mockito.any(String.class));
  }

  @RepeatedTest(5)
  @DisplayName("getMd5 should not always return the same value across independent sessions (SecureRandom)")
  void getMd5UsesSecureRandomForSecretSelection() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    String previousHash = null;

    for (int i = 0; i < 5; i++) {
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      HttpSession session = Mockito.mock(HttpSession.class);
      Mockito.when(request.getSession()).thenReturn(session);
      Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);

      String hash = assignment.getMd5(request);

      assertNotNull(hash, "Hash should not be null");
      if (previousHash != null) {
        // With SecureRandom the likelihood of constant repetition is very low; this
        // check helps assert that we are not using a deterministic weak source.
        if (!hash.equals(previousHash)) {
          return; // diversity observed, test passes early
        }
      }
      previousHash = hash;
    }

    // If all generated hashes are identical across all runs, treat this as suspicious.
    // Note: This is a probabilistic check targeting the change from Random to SecureRandom.
    assertNotEquals(
        previousHash,
        assignment.getMd5(mockRequestWithEmptySession()),
        "Expected SecureRandom-backed selection to provide diversity across calls");
  }

  private HttpServletRequest mockRequestWithEmptySession() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);
    return request;
  }

  @Test
  @DisplayName("getSha256 should store secret and hash in session on first call")
  void getSha256StoresValuesInSession() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("sha256")).thenReturn(null);

    assignment.getSha256(request);

    Mockito.verify(session).setAttribute(Mockito.eq("sha256Hash"), Mockito.any(String.class));
    Mockito.verify(session).setAttribute(Mockito.eq("sha256Secret"), Mockito.any(String.class));
  }
}
