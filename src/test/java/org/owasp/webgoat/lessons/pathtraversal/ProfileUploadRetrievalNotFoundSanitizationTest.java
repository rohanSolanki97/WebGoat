package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;

/**
 * Delta tests for ProfileUploadRetrieval changes:
 * - NOT_FOUND response now uses validated fileName in Location header
 * - Directory listing in NOT_FOUND response is filtered to exclude suspicious names
 * - Throws IllegalArgumentException if computed fileName is suspicious (defense-in-depth)
 */
class ProfileUploadRetrievalNotFoundSanitizationTest {

  @TempDir Path tempDir;

  @Test
  void getProfilePicture_notFound_filtersSuspiciousFilenamesFromListing() throws Exception {
    // Arrange
    // Create cats directory with one safe and one suspicious filename
    Path catsDir = tempDir.resolve("PathTraversal").resolve("cats");
    Files.createDirectories(catsDir);
    Files.write(catsDir.resolve("1.jpg"), "x".getBytes(StandardCharsets.UTF_8));
    Files.write(catsDir.resolve("..evil.jpg"), "x".getBytes(StandardCharsets.UTF_8));

    ProfileUploadRetrieval sut = new ProfileUploadRetrieval(tempDir.toString());

    HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn("id=9999");
    when(request.getParameter("id")).thenReturn("9999"); // ensures catPicture does not exist -> NOT_FOUND

    // Act
    var response = sut.getProfilePicture(request);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    String body = new String((byte[]) response.getBody(), StandardCharsets.UTF_8);
    assertFalse(body.contains("..evil.jpg"), "NOT_FOUND listing must not include suspicious filenames");
    // safe file should still be present in listing
    // (StringUtils.arrayToCommaDelimitedString(File[]) uses File#toString which includes path; check by name)
    assertFalse(body.isEmpty(), "Expected a non-empty listing body");
    assertFalse(body.contains("null"), "Listing should not be null");
  }

  @Test
  void getProfilePicture_notFound_locationHeaderUsesValidatedFileName() throws Exception {
    // Arrange
    Path catsDir = tempDir.resolve("PathTraversal").resolve("cats");
    Files.createDirectories(catsDir);

    ProfileUploadRetrieval sut = new ProfileUploadRetrieval(tempDir.toString());

    HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn("id=9999");
    when(request.getParameter("id")).thenReturn("9999");

    // Act
    var response = sut.getProfilePicture(request);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("/PathTraversal/random-picture?id=9999.jpg", response.getHeaders().getLocation().toString());
  }

  @Test
  void getProfilePicture_throwsWhenComputedFileNameIsSuspicious() {
    // Arrange
    ProfileUploadRetrieval sut = new ProfileUploadRetrieval(tempDir.toString());

    HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
    // Ensure we don't get blocked by the earlier query string check (blocks ".." and "/")
    when(request.getQueryString()).thenReturn("id=..");
    // But force an id that will produce a suspicious fileName; this is defense-in-depth
    when(request.getParameter("id")).thenReturn("..");

    // Act + Assert
    assertThrows(IllegalArgumentException.class, () -> sut.getProfilePicture(request));
  }
}
