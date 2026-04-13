package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

/**
 * Delta tests for HashingAssignment focusing on the vulnerability:
 * "Use of Cryptographically Weak Pseudo-Random Number Generator".
 *
 * These tests ensure that:
 * - The session-based caching behavior is preserved (hash is stable once in session).
 * - The public API of getMd5/getSha256 remains unchanged after switching to SecureRandom.
 *
 * Note: The actual use of java.security.SecureRandom is a private implementation detail, so we
 * avoid probabilistic checks that would be flaky and instead validate observable behavior.
 */
class HashingAssignmentTest {

  @Test
  void getMd5_usesSessionCachedHashWhenPresent() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    String existingHash = "ABCDEF0123456789";
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Hash")).thenReturn(existingHash);

    // Act
    String result = assignment.getMd5(request);

    // Assert
    // Core behavior: if the hash is already present in the session, it must be reused.
    assertEquals(existingHash, result, "Expected getMd5 to reuse the md5Hash from the session");
  }

  @Test
  void getSha256_usesSessionCachedHashWhenPresent() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    String existingHash = "FEDCBA9876543210";
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("sha256")).thenReturn(existingHash);

    // Act
    String result = assignment.getSha256(request);

    // Assert
    assertEquals(existingHash, result, "Expected getSha256 to reuse the sha256 hash from session");
  }
}
