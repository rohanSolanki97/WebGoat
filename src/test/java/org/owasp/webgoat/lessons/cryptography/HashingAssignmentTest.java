package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * Resolved test path (derived from src/main/java → src/test/java):
 * src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
 */
public class HashingAssignmentTest {

  @Test
  @DisplayName("getMd5 generates a hash and stores both hash and secret in session on first call")
  void getMd5_generatesAndCachesHash() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String hash = assignment.getMd5(request);

    // Assert
    assertNotNull(hash, "MD5 hash should not be null");
    verify(session).setAttribute(eq("md5Hash"), anyString());
    verify(session).setAttribute(eq("md5Secret"), anyString());
  }

  @Test
  @DisplayName("getMd5 typically produces different hashes across independent sessions (indicative of SecureRandom)")
  void getMd5_usesNonDeterministicSecretSelection() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request1 = mock(HttpServletRequest.class);
    HttpServletRequest request2 = mock(HttpServletRequest.class);
    HttpSession session1 = mock(HttpSession.class);
    HttpSession session2 = mock(HttpSession.class);

    when(request1.getSession()).thenReturn(session1);
    when(request2.getSession()).thenReturn(session2);
    when(session1.getAttribute("md5Hash")).thenReturn(null);
    when(session2.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String hash1 = assignment.getMd5(request1);
    String hash2 = assignment.getMd5(request2);

    // Assert
    assertNotNull(hash1);
    assertNotNull(hash2);
    // Behavioral check: with SecureRandom-based secret selection, it's unlikely two independent
    // sessions will get the same secret/hash. This acts as a regression signal if randomness
    // becomes deterministic again.
    assertNotEquals(
        hash1, hash2, "Two independent MD5 hashes should typically differ when using SecureRandom");
  }
}
