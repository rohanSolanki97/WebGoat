package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProfileUploadRetrievalTest {

  @TempDir File tempDir;

  @Test
  void getProfilePicture_notFoundBranch_rejectsTraversalLikeFileName() {
    // Arrange
    ProfileUploadRetrieval sut = new ProfileUploadRetrieval(tempDir.getAbsolutePath());
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn("id=9999");
    when(request.getParameter("id")).thenReturn("9999");

    // Act + Assert
    assertThrows(IllegalArgumentException.class, () -> sut.getProfilePicture(request));
  }

  @Test
  void getProfilePicture_notFoundBranch_returnsNotFoundAndDoesNotThrowForNumericId() {
    // Arrange
    ProfileUploadRetrieval sut = new ProfileUploadRetrieval(tempDir.getAbsolutePath());
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn("id=9999");
    when(request.getParameter("id")).thenReturn("9999");

    // Act
    ResponseEntity<?> response = sut.getProfilePicture(request);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getHeaders().getLocation());
    assertTrue(response.getHeaders().getLocation().toString().contains("id="));

    assertTrue(response.getBody() instanceof byte[]);
    String body = new String((byte[]) response.getBody(), StandardCharsets.UTF_8);
    assertNotNull(body);
  }
}
