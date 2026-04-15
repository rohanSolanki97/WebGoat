package org.owasp.webgoat.lessons.cryptography;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Delta unit test file for:
 *   batch_id: BATCH-001
 *   resolved_file_path: src/main/java/org/owasp/webgoat/lessons/cryptography/HashingAssignment.java
 *   derived_test_file_path: src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
 *
 * Focus: behavior affected by switching from java.util.Random to java.security.SecureRandom
 * when selecting a secret from SECRETS for hashing.
 */
class HashingAssignmentTest {

  @Test
  void getMd5_shouldInitializeSessionWithSecretFromAllowedSetAndStoreHashOnce()
      throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Hash")).thenReturn(null);

    ArgumentCaptor<Object> hashCaptor = ArgumentCaptor.forClass(Object.class);
    ArgumentCaptor<Object> secretCaptor = ArgumentCaptor.forClass(Object.class);

    // Act
    String returnedHash = assignment.getMd5(request);

    // Assert
    Mockito.verify(session).setAttribute(Mockito.eq("md5Hash"), hashCaptor.capture());
    Object storedHash = hashCaptor.getValue();
    assertThat(storedHash).isInstanceOf(String.class);
    assertThat((String) storedHash).isEqualTo(returnedHash);

    Mockito.verify(session).setAttribute(Mockito.eq("md5Secret"), secretCaptor.capture());
    Object storedSecret = secretCaptor.getValue();
    assertThat(storedSecret).isInstanceOf(String.class);
    assertThat((String) storedSecret).isIn(HashingAssignment.SECRETS);
  }

  @Test
  void getSha256_shouldInitializeSessionWithSecretFromAllowedSetAndStoreHashOnce()
      throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("sha256")).thenReturn(null);

    ArgumentCaptor<Object> hashCaptor = ArgumentCaptor.forClass(Object.class);
    ArgumentCaptor<Object> secretCaptor = ArgumentCaptor.forClass(Object.class);

    // Act
    String returnedHash = assignment.getSha256(request);

    // Assert
    Mockito.verify(session).setAttribute(Mockito.eq("sha256Hash"), hashCaptor.capture());
    Object storedHash = hashCaptor.getValue();
    assertThat(storedHash).isInstanceOf(String.class);
    assertThat((String) storedHash).isEqualTo(returnedHash);

    Mockito.verify(session).setAttribute(Mockito.eq("sha256Secret"), secretCaptor.capture());
    Object storedSecret = secretCaptor.getValue();
    assertThat(storedSecret).isInstanceOf(String.class);
    assertThat((String) storedSecret).isIn(HashingAssignment.SECRETS);
  }
}
