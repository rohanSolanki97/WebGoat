package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Delta tests for HashingAssignment focusing on the changed randomness source in getMd5().
 *
 * Test file path (derived): src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
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
    when(request.getSession()).thenReturn(session);
  }

  @Test
  void getMd5_generatesAndStoresHashWhenNotPresent() throws NoSuchAlgorithmException {
    when(session.getAttribute("md5Hash")).thenReturn(null);

    String hash = hashingAssignment.getMd5(request);

    Mockito.verify(session).setAttribute(Mockito.eq("md5Hash"), Mockito.anyString());
    Mockito.verify(session).setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());
    assertNotNull(hash);
  }

  @Test
  void getMd5_returnsExistingHashWithoutRegenerating() throws NoSuchAlgorithmException {
    when(session.getAttribute("md5Hash")).thenReturn("EXISTING_HASH");

    String hash = hashingAssignment.getMd5(request);

    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("md5Hash"), Mockito.anyString());
    Mockito.verify(session, Mockito.never())
        .setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());
    assertNotNull(hash);
  }
}
