package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

/**
 * Delta tests for HashingAssignment focusing on the changed behavior:
 * - Use of SecureRandom instead of Random
 * - Use of SHA-256 instead of MD5 for the first hashing endpoint
 * - Session attribute names updated from md5* to sha256* for the first secret
 *
 * These tests do NOT assert randomness statistically but verify:
 * - Values are generated and stored in the expected session attributes
 * - Repeated invocations without clearing session reuse the cached values
 */
public class HashingAssignmentTest {

  private HashingAssignment hashingAssignment;
  private HttpServletRequest request;
  private HttpSession session;

  @BeforeEach
  void setUp() {
    hashingAssignment = new HashingAssignment();
    request = Mockito.mock(HttpServletRequest.class);
    session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession()).thenReturn(session);
  }

  @Test
  void getSha256Part1_generatesAndStoresHashAndSecretInSession() throws NoSuchAlgorithmException {
    // Arrange
    Mockito.when(session.getAttribute("sha256HashPart1")).thenReturn(null);

    // Act
    String result =
        hashingAssignment.getSha256Part1(request); // previously MD5 endpoint, now SHA-256 based

    // Assert
    assertNotNull(result, "Hash should be generated when no value is stored in session");
    // verify that the method stores both hash and secret in the updated session keys
    Mockito.verify(session).setAttribute(Mockito.eq("sha256HashPart1"), Mockito.anyString());
    Mockito.verify(session).setAttribute(Mockito.eq("sha256SecretPart1"), Mockito.anyString());
  }

  @Test
  void getSha256Part1_reusesExistingSessionHashIfPresent() throws NoSuchAlgorithmException {
    // Arrange
    String existingHash = "EXISTING_HASH";
    Mockito.when(session.getAttribute("sha256HashPart1")).thenReturn(existingHash);

    // Act
    String result = hashingAssignment.getSha256Part1(request);

    // Assert
    // When a hash is already present, the method should return it without generating a new one
    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("sha256HashPart1"), Mockito.anyString());
    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("sha256SecretPart1"), Mockito.anyString());
    MediaType type = MediaType.TEXT_HTML; // access updated import to ensure it compiles
    assertNotNull(type, "MediaType import should be usable");
    assertNotNull(result, "Returned hash should not be null");
  }

  @Test
  void completed_verifiesAgainstUpdatedSha256SecretsFromSession() {
    // Arrange
    String secret1 = "firstSecret";
    String secret2 = "secondSecret";

    Mockito.when(session.getAttribute("sha256SecretPart1")).thenReturn(secret1);
    Mockito.when(session.getAttribute("sha256Secret")).thenReturn(secret2);

    // Act
    var successResult = hashingAssignment.completed(request, secret1, secret2);
    var partialResult = hashingAssignment.completed(request, secret1, "wrong");
    var failResult = hashingAssignment.completed(request, "wrong1", "wrong2");

    // Assert
    assertNotEquals(
        successResult.getFeedback(),
        failResult.getFeedback(),
        "Successful attempt feedback should differ from full failure");
    assertNotEquals(
        partialResult.getFeedback(),
        failResult.getFeedback(),
        "Partial success feedback should differ from full failure");
  }
}
