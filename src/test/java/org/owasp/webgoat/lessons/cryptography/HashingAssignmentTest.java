package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Delta tests for HashingAssignment focusing on the switch from Random to SecureRandom.
 */
class HashingAssignmentTest {

  @Test
  void getMd5_usesSecureRandomToSelectSecretAndStoresHashInSession()
      throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Hash")).thenReturn(null);

    try (MockedStatic<SecureRandom> secureRandomStatic = mockStatic(SecureRandom.class)) {
      SecureRandom secureRandom = mock(SecureRandom.class);
      secureRandomStatic.when(SecureRandom::getInstanceStrong).thenReturn(secureRandom);
      when(secureRandom.nextInt(HashingAssignment.SECRETS.length)).thenReturn(0);

      String expectedSecret = HashingAssignment.SECRETS[0];
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(expectedSecret.getBytes());
      String expectedHash =
          DatatypeConverter.printHexBinary(md.digest()).toUpperCase();

      String actual = assignment.getMd5(request);

      assertEquals(expectedHash, actual);
      verify(session).setAttribute("md5Hash", expectedHash);
      verify(session).setAttribute("md5Secret", expectedSecret);
    }
  }

  @Test
  void getSha256_usesSecureRandomToSelectSecretAndStoresHashInSession()
      throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("sha256")).thenReturn(null);

    try (MockedStatic<SecureRandom> secureRandomStatic = mockStatic(SecureRandom.class)) {
      SecureRandom secureRandom = mock(SecureRandom.class);
      secureRandomStatic.when(SecureRandom::getInstanceStrong).thenReturn(secureRandom);
      when(secureRandom.nextInt(HashingAssignment.SECRETS.length)).thenReturn(1);

      String expectedSecret = HashingAssignment.SECRETS[1];
      String expectedHash = HashingAssignment.getHash(expectedSecret, "SHA-256");

      String actual = assignment.getSha256(request);

      assertEquals(expectedHash, actual);
      verify(session).setAttribute("sha256Hash", expectedHash);
      verify(session).setAttribute("sha256Secret", expectedSecret);
    }
  }
}
