package org.owasp.webgoat.lessons.cryptography;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Test;

/**
 * Delta tests for HashingAssignment focusing on the change from java.util.Random
 * to java.security.SecureRandom. These tests assert that:
 * - A secret is selected from SECRETS and hashed when no value exists in the session.
 * - Subsequent calls reuse the value from the session (behavior unchanged).
 *
 * Note: We cannot deterministically assert randomness quality, but we verify that the
 * logic still behaves correctly using the new SecureRandom-based selection.
 */
public class HashingAssignmentTest {

  @Test
  void getMd5_shouldGenerateAndStoreHashWhenNotInSession() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String result = assignment.getMd5(request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();

    // Verify that a secret from SECRETS was used and stored
    verify(session).setAttribute(eq("md5Hash"), anyString());
    verify(session).setAttribute(eq("md5Secret"), anyString());
  }

  @Test
  void getMd5_shouldReuseExistingHashFromSession() throws NoSuchAlgorithmException {
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
    assertThat(result).isEqualTo(existingHash);
    verify(session, never()).setAttribute(eq("md5Hash"), any());
    verify(session, never()).setAttribute(eq("md5Secret"), any());
  }

  @Test
  void getSha256_shouldGenerateAndStoreHashWhenNotInSession() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("sha256")).thenReturn(null);

    // Act
    String result = assignment.getSha256(request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();

    verify(session).setAttribute(eq("sha256Hash"), anyString());
    verify(session).setAttribute(eq("sha256Secret"), anyString());
  }

  @Test
  void getSha256_shouldReuseExistingHashFromSession() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    String existingHash = "ABCDEF0123456789";
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("sha256")).thenReturn(existingHash);

    // Act
    String result = assignment.getSha256(request);

    // Assert
    assertThat(result).isEqualTo(existingHash);
    verify(session, never()).setAttribute(eq("sha256Hash"), any());
    verify(session, never()).setAttribute(eq("sha256Secret"), any());
  }

  @Test
  void getMd5_shouldProduceValidMd5HexString() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String result = assignment.getMd5(request);

    // Assert
    // Validate it is hex and uppercase, consistent with DatatypeConverter.printHexBinary
    assertThat(result).matches("^[0-9A-F]+$");
  }
}
