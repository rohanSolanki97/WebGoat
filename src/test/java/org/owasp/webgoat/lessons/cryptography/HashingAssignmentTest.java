package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HashingAssignmentTest {

  @Test
  void getMd5_usesExistingSessionHashWithoutRegeneratingSecret() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    String existingHash = "ABCDEF123456";
    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(existingHash);

    String result = assignment.getMd5(request);

    assertEquals(existingHash, result);
    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("md5Hash"), Mockito.any());
    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("md5Secret"), Mockito.any());
  }

  @Test
  void getMd5_generatesNonNullHashAndStoresSecretAndHashOnce() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);

    String hash = assignment.getMd5(request);

    assertNotNull(hash);
    Mockito.verify(session).setAttribute(Mockito.eq("md5Hash"), Mockito.eq(hash));
    Mockito.verify(session).setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());
  }

  @Test
  void getSha256_generatesNonNullHashAndStoresSecretAndHashOnce() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("sha256")).thenReturn(null);

    String hash = assignment.getSha256(request);

    assertNotNull(hash);
    Mockito.verify(session).setAttribute(Mockito.eq("sha256Hash"), Mockito.eq(hash));
    Mockito.verify(session).setAttribute(Mockito.eq("sha256Secret"), Mockito.anyString());
  }
}
