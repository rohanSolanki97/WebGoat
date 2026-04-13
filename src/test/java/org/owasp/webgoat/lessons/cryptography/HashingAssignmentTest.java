package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for HashingAssignment focusing on behavior around the changed random secret
 * selection (Random -> SecureRandom). Since the implementation detail is not exposed,
 * these tests validate behavior at the public API level while assuming the SecureRandom
 * fix is internal.
 */
class HashingAssignmentTest {

  @Test
  void getMd5_returnsSameHashWhenSessionHasCachedValue() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Hash")).thenReturn("CACHED_HASH");

    String hash = assignment.getMd5(request);

    assertEquals("CACHED_HASH", hash);
    // No new secret should be generated or stored when a hash is already cached
    verify(session, never()).setAttribute(eq("md5Hash"), any());
    verify(session, never()).setAttribute(eq("md5Secret"), any());
  }

  @Test
  void getMd5_generatesAndStoresNewHashWhenNotCached() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Hash")).thenReturn(null);

    String hash = assignment.getMd5(request);

    assertNotNull(hash);
    assertFalse(hash.isEmpty());
    verify(session).setAttribute(eq("md5Hash"), anyString());
    verify(session).setAttribute(eq("md5Secret"), anyString());
  }

  @Test
  void getSha256_generatesAndStoresNewHashWhenNotCached() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("sha256")).thenReturn(null);

    String hash = assignment.getSha256(request);

    assertNotNull(hash);
    assertFalse(hash.isEmpty());
    verify(session).setAttribute(eq("sha256Hash"), anyString());
    verify(session).setAttribute(eq("sha256Secret"), anyString());
  }

  @Test
  void completed_requiresBothSecretsToMatch() {
    HashingAssignment assignment = new HashingAssignment();

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("md5Secret")).thenReturn("one");
    when(session.getAttribute("sha256Secret")).thenReturn("two");

    AttackResult resultOk =
        assignment.completed(request, "one", "two");
    assertTrue(resultOk.getLessonCompleted());

    AttackResult resultOneOk =
        assignment.completed(request, "one", "wrong");
    assertFalse(resultOneOk.getLessonCompleted());

    AttackResult resultEmpty =
        assignment.completed(request, null, null);
    assertFalse(resultEmpty.getLessonCompleted());
  }
}
