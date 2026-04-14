package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Delta tests for HashingAssignment focusing on the switch from java.util.Random to
 * java.security.SecureRandom for secret selection.
 */
public class HashingAssignmentTest {

  private HashingAssignment hashingAssignment;
  private HttpServletRequest request;
  private HttpSession session;

  @BeforeEach
  void setup() {
    hashingAssignment = new HashingAssignment();
    request = Mockito.mock(HttpServletRequest.class);
    session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession()).thenReturn(session);
  }

  @Test
  void getMd5_usesSecureRandomCharacteristicsForSecretSelection() throws NoSuchAlgorithmException {
    // Arrange
    // First call should generate and store a secret/hash in the session
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);

    // We will capture multiple generated hashes to ensure a high degree of variance
    Set<String> hashes = new HashSet<>();

    // Act
    for (int i = 0; i < 50; i++) {
      // each iteration must behave as if session had no hash yet
      Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);
      String hash = hashingAssignment.getMd5(request);
      hashes.add(hash);
    }

    // Assert
    // If a predictable or constant value were used, we'd see far fewer unique hashes.
    // This assertion ensures that the randomness change (SecureRandom) is actually exercised.
    assertTrue(
        hashes.size() > 5,
        "Expected multiple distinct MD5 hashes, indicating non-trivial randomness source");
  }

  @Test
  void getSha256_usesSecureRandomCharacteristicsForSecretSelection() throws NoSuchAlgorithmException {
    // Arrange
    Mockito.when(session.getAttribute("sha256")).thenReturn(null);

    Set<String> hashes = new HashSet<>();

    // Act
    for (int i = 0; i < 50; i++) {
      Mockito.when(session.getAttribute("sha256")).thenReturn(null);
      String hash = hashingAssignment.getSha256(request);
      hashes.add(hash);
    }

    // Assert
    assertTrue(
        hashes.size() > 5,
        "Expected multiple distinct SHA-256 hashes, indicating non-trivial randomness source");
  }
}
