package org.owasp.webgoat.lessons.cryptography;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Delta tests for HashingAssignment focusing on the change from java.util.Random to SecureRandom,
 * verified via observable behavior through the public endpoints.
 */
class HashingAssignmentTest {

  @Test
  void getMd5_shouldReuseSessionHashWithoutGeneratingNewSecret() throws Exception {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    // First call: no attributes yet
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String firstHash = assignment.getMd5(request);

    // Assert: first call sets both hash and secret
    Mockito.verify(session).setAttribute(Mockito.eq("md5Hash"), Mockito.anyString());
    Mockito.verify(session).setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());

    // Prepare second call where hash is already present, so no new random secret should be chosen
    Mockito.reset(session);
    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(firstHash);

    // Act
    String secondHash = assignment.getMd5(request);

    // Assert: no new attributes written and same hash returned
    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("md5Hash"), Mockito.anyString());
    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());
    org.junit.jupiter.api.Assertions.assertEquals(firstHash, secondHash);
  }

  @Test
  void getSha256_shouldStoreSecretAndHashInSession() throws NoSuchAlgorithmException {
    // This test exercises the second code path using SecureRandom for secret selection.
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("sha256")).thenReturn(null);

    String hash = assignment.getSha256(request);

    Mockito.verify(session).setAttribute(Mockito.eq("sha256Hash"), Mockito.eq(hash));
    Mockito.verify(session).setAttribute(Mockito.eq("sha256Secret"), Mockito.anyString());
  }
}
