package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Delta tests for id validation in ProfileUploadRetrieval.getProfilePicture:
 * - id must be digits only, otherwise IllegalArgumentException is thrown.
 */
class ProfileUploadRetrievalTest {

  @TempDir Path tempDir;

  @Test
  void getProfilePicture_rejectsNonNumericId() {
    // Arrange
    ProfileUploadRetrieval sut = new ProfileUploadRetrieval(tempDir.toString());
    HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn("id=../secret");
    // query string check blocks ".." and "/" before id parsing; use a value that passes that check
    when(request.getQueryString()).thenReturn("id=abc");
    when(request.getParameter("id")).thenReturn("abc");

    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> sut.getProfilePicture(request),
        "Non-numeric id must be rejected");
  }

  @Test
  void getProfilePicture_rejectsIdContainingMixedCharacters() {
    // Arrange
    ProfileUploadRetrieval sut = new ProfileUploadRetrieval(tempDir.toString());
    HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn("id=12a3");
    when(request.getParameter("id")).thenReturn("12a3");

    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> sut.getProfilePicture(request),
        "Mixed alphanumeric id must be rejected");
  }
}
