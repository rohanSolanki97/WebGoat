package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Delta tests for HashingAssignment focusing on the change from java.util.Random to
 * java.security.SecureRandom when selecting secrets.
 *
 * These tests verify that:
 * - Initialization of hashes and secrets still works.
 * - Subsequent calls use the cached values and do not reinitialize.
 *
 * Note: We do not test randomness distribution here to avoid probabilistic/brittle tests.
 */
public class HashingAssignmentTest {

  @Test
  void getMd5_initializesHashAndSecretWhenNotPresent() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String result = assignment.getMd5(request);

    // Assert
    assertNotNull(result, "MD5 hash should be initialized and not null");
    Mockito.verify(session).setAttribute(Mockito.eq("md5Hash"), Mockito.anyString());
    Mockito.verify(session).setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());
  }

  @Test
  void getMd5_returnsCachedHashOnSubsequentCalls() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn("CACHED_HASH");

    // Act
    String result = assignment.getMd5(request);

    // Assert
    assertEquals("CACHED_HASH", result, "When a hash is cached it should be returned as-is");
    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());
  }

  @Test
  void getSha256_initializesHashAndSecretWhenNotPresent() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("sha256")).thenReturn(null);

    // Act
    String result = assignment.getSha256(request);

    // Assert
    assertNotNull(result, "SHA-256 hash should be initialized and not null");
    Mockito.verify(session).setAttribute(Mockito.eq("sha256Hash"), Mockito.anyString());
    Mockito.verify(session).setAttribute(Mockito.eq("sha256Secret"), Mockito.anyString());
  }
}
