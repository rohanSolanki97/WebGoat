package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Delta test focusing on the change from java.util.Random to java.security.SecureRandom:
 * verifies that different sessions are unlikely to receive the same hash.
 */
public class HashingAssignmentTest {

  @Test
  void getMd5_returnsDifferentHashesForDifferentSessions() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request1 = Mockito.mock(HttpServletRequest.class);
    HttpServletRequest request2 = Mockito.mock(HttpServletRequest.class);
    HttpSession session1 = Mockito.mock(HttpSession.class);
    HttpSession session2 = Mockito.mock(HttpSession.class);

    Mockito.when(request1.getSession()).thenReturn(session1);
    Mockito.when(request2.getSession()).thenReturn(session2);
    Mockito.when(session1.getAttribute("md5Hash")).thenReturn(null);
    Mockito.when(session2.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String hash1 = assignment.getMd5(request1);
    String hash2 = assignment.getMd5(request2);

    // Assert
    // With SecureRandom, the probability that two independent sessions get the same secret
    // (and thus the same hash) is low; this assertion focuses on the new randomness behavior.
    assertNotEquals(hash1, hash2);
  }
}
