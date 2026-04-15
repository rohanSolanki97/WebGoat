// File: src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
// Derived from src/main/java/org/owasp/webgoat/lessons/cryptography/HashingAssignment.java
package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HashingAssignmentTest {

  @Test
  void getMd5_generatesAndStoresHashWhenNotPresent() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(null);

    // Act
    String firstCall = assignment.getMd5(request);

    // Assert
    assertNotNull(firstCall);
    Mockito.verify(session).setAttribute("md5Hash", firstCall);
  }

  @Test
  void getMd5_reusesExistingHashAndDoesNotRegenerate() throws NoSuchAlgorithmException {
    // Arrange
    HashingAssignment assignment = new HashingAssignment();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);

    String existingHash = "ABCDEF012345";
    Mockito.when(request.getSession()).thenReturn(session);
    Mockito.when(session.getAttribute("md5Hash")).thenReturn(existingHash);

    // Act
    String result = assignment.getMd5(request);

    // Assert
    assertEquals(existingHash, result);
    // Ensure no new value is written when one already exists
    Mockito.verify(session, Mockito.never()).setAttribute(Mockito.eq("md5Hash"), Mockito.any());
  }
}
