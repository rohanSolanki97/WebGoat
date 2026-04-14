package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/*
 * Delta test for:
 *   Source: src/main/java/org/owasp/webgoat/lessons/cryptography/HashingAssignment.java
 *   Test:   src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
 *
 * Focus: behavior around random secret selection and session caching after switching
 * from java.util.Random to java.security.SecureRandom.
 */
public class HashingAssignmentTest {

  private HashingAssignment hashingAssignment;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;

  @BeforeEach
  void setup() {
    hashingAssignment = new HashingAssignment();
    requestMock = Mockito.mock(HttpServletRequest.class);
    sessionMock = Mockito.mock(HttpSession.class);
    Mockito.when(requestMock.getSession()).thenReturn(sessionMock);
  }

  @Test
  void getMd5ShouldCacheHashAndSecretInSession() throws NoSuchAlgorithmException {
    // Arrange: first call, no attributes in session
    Mockito.when(sessionMock.getAttribute("md5Hash")).thenReturn(null);

    Mockito.doAnswer(
            invocation -> {
              String key = invocation.getArgument(0, String.class);
              Object value = invocation.getArgument(1);
              if ("md5Hash".equals(key)) {
                Mockito.when(sessionMock.getAttribute("md5Hash")).thenReturn(value);
              }
              if ("md5Secret".equals(key)) {
                Mockito.when(sessionMock.getAttribute("md5Secret")).thenReturn(value);
              }
              return null;
            })
        .when(sessionMock)
        .setAttribute(Mockito.anyString(), Mockito.any());

    // Act: first call computes and stores hash, second call should reuse it
    String first = hashingAssignment.getMd5(requestMock);
    String second = hashingAssignment.getMd5(requestMock);

    // Assert: hash and secret are cached and stable
    assertNotNull(first, "MD5 hash should be computed on first call");
    assertEquals(first, second, "MD5 hash should be cached and reused from session");
    assertNotNull(
        sessionMock.getAttribute("md5Secret"),
        "Secret used for MD5 hashing should be stored in the session");
  }

  @Test
  void getSha256ShouldCacheHashAndSecretInSession() throws Exception {
    // Arrange: first call, no attributes in session
    Mockito.when(sessionMock.getAttribute("sha256")).thenReturn(null);

    Mockito.doAnswer(
            invocation -> {
              String key = invocation.getArgument(0, String.class);
              Object value = invocation.getArgument(1);
              if ("sha256Hash".equals(key)) {
                Mockito.when(sessionMock.getAttribute("sha256")).thenReturn(value);
              }
              if ("sha256Secret".equals(key)) {
                Mockito.when(sessionMock.getAttribute("sha256Secret")).thenReturn(value);
              }
              return null;
            })
        .when(sessionMock)
        .setAttribute(Mockito.anyString(), Mockito.any());

    // Act
    String first = hashingAssignment.getSha256(requestMock);
    String second = hashingAssignment.getSha256(requestMock);

    // Assert
    assertNotNull(first, "SHA-256 hash should be computed on first call");
    assertEquals(first, second, "SHA-256 hash should be cached and reused from session");
    assertNotNull(
        sessionMock.getAttribute("sha256Secret"),
        "Secret used for SHA-256 hashing should be stored in the session");
  }
}
