package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * File path (derived from main source): src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
 *
 * Delta tests for HashingAssignment focusing on the switch from Random to SecureRandom while
 * preserving the externally visible behavior (session caching and lesson completion).
 */
class HashingAssignmentTest {

  private HttpServletRequest mockRequestWithNewSession() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession()).thenReturn(session);
    return request;
  }

  @Test
  void getMd5_usesSessionCachingAndDoesNotRegenerateSecretOnceSet() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mockRequestWithNewSession();
    HttpSession session = request.getSession();

    // First call: generates and stores secret+hash in session using SecureRandom
    String firstHash = assignment.getMd5(request);

    Mockito.verify(session).setAttribute(Mockito.eq("md5Hash"), Mockito.eq(firstHash));
    Mockito.verify(session).setAttribute(Mockito.eq("md5Secret"), Mockito.anyString());

    // Second call with hash already present in session must not re-randomize
    Mockito.reset(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(firstHash);

    String secondHash = assignment.getMd5(request);

    assertEquals(firstHash, secondHash, "Hash should be stable once cached in session");
    Mockito.verify(session, Mockito.never()).setAttribute(Mockito.eq("md5Hash"), Mockito.any());
    Mockito.verify(session, Mockito.never()).setAttribute(Mockito.eq("md5Secret"), Mockito.any());
  }

  @Test
  void getMd5_producesDifferentHashesForDifferentSessions() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();

    // Instead of asserting exact randomness properties, just assert that different
    // sessions are not forced to share the same secret/hash, which is the intended
    // behavior after switching to SecureRandom.
    Set<String> hashes = new HashSet<>();

    for (int i = 0; i < 2; i++) {
      HttpServletRequest request = mockRequestWithNewSession();
      String hash = assignment.getMd5(request);
      hashes.add(hash);
    }

    assertNotEquals(
        1,
        hashes.size(),
        "Different sessions should be capable of having different hashes (no global fixed secret)");
  }

  @Test
  void getSha256_usesSessionCachingAndDoesNotRegenerateSecretOnceSet() throws NoSuchAlgorithmException {
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = mockRequestWithNewSession();
    HttpSession session = request.getSession();

    String firstHash = assignment.getSha256(request);

    Mockito.verify(session).setAttribute(Mockito.eq("sha256Hash"), Mockito.eq(firstHash));
    Mockito.verify(session).setAttribute(Mockito.eq("sha256Secret"), Mockito.anyString());

    Mockito.reset(session);
    Mockito.when(session.getAttribute("sha256")).thenReturn(firstHash);

    String secondHash = assignment.getSha256(request);

    assertEquals(firstHash, secondHash, "SHA-256 hash should be stable once cached in session");
    Mockito.verify(session, Mockito.never()).setAttribute(Mockito.eq("sha256Hash"), Mockito.any());
    Mockito.verify(session, Mockito.never()).setAttribute(Mockito.eq("sha256Secret"), Mockito.any());
  }

  @Test
  void completed_succeedsWhenSecretsMatchSessionValues_afterRandomSelection() {
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession()).thenReturn(session);

    Mockito.when(session.getAttribute("md5Secret")).thenReturn("secret1");
    Mockito.when(session.getAttribute("sha256Secret")).thenReturn("secret2");

    AttackResult result = assignment.completed(request, "secret1", "secret2");

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getLessonCompleted())
        .as("Lesson completion behavior must remain unchanged after switching to SecureRandom")
        .isTrue();
  }
}
