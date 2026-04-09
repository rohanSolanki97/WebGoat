package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Delta tests for HashingAssignment focusing on the change to SecureRandom-based secret selection.
 */
public class HashingAssignmentTest {

  @Test
  void getMd5_shouldTypicallyYieldDifferentHashesForSeparateSessions() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment hashingAssignment = new HashingAssignment();
    HttpServletRequest request1 = Mockito.mock(HttpServletRequest.class);
    HttpServletRequest request2 = Mockito.mock(HttpServletRequest.class);
    HttpSession session1 = Mockito.mock(HttpSession.class);
    HttpSession session2 = Mockito.mock(HttpSession.class);

    Mockito.when(request1.getSession()).thenReturn(session1);
    Mockito.when(request2.getSession()).thenReturn(session2);
    Mockito.when(session1.getAttribute("md5Hash")).thenReturn(null);
    Mockito.when(session2.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String hash1 = hashingAssignment.getMd5(request1);
    String hash2 = hashingAssignment.getMd5(request2);

    // Assert
    assertNotNull(hash1);
    assertNotNull(hash2);
    // Not a proof, but indicates non-deterministic, SecureRandom-backed secret selection.
    assertNotEquals(
        hash1,
        hash2,
        "Independent MD5 values for separate sessions should typically differ when using SecureRandom");
  }

  @Test
  void getSha256_shouldTypicallyYieldDifferentHashesForSeparateSessions() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment hashingAssignment = new HashingAssignment();
    HttpServletRequest request1 = Mockito.mock(HttpServletRequest.class);
    HttpServletRequest request2 = Mockito.mock(HttpServletRequest.class);
    HttpSession session1 = Mockito.mock(HttpSession.class);
    HttpSession session2 = Mockito.mock(HttpSession.class);

    Mockito.when(request1.getSession()).thenReturn(session1);
    Mockito.when(request2.getSession()).thenReturn(session2);
    Mockito.when(session1.getAttribute("sha256")).thenReturn(null);
    Mockito.when(session2.getAttribute("sha256")).thenReturn(null);

    // Act
    String hash1 = hashingAssignment.getSha256(request1);
    String hash2 = hashingAssignment.getSha256(request2);

    // Assert
    assertNotNull(hash1);
    assertNotNull(hash2);
    assertNotEquals(
        hash1,
        hash2,
        "Independent SHA-256 values for separate sessions should typically differ when using SecureRandom");
  }
}
