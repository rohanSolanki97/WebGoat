package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Delta tests for username traversal validation in ProfileUploadBase.getProfilePictureAsBase64.
 */
class ProfileUploadBaseGetProfilePictureAsBase64Test {

  @TempDir Path tempDir;

  @Test
  void getProfilePictureAsBase64_rejectsUsernameContainingDotDot() {
    // Arrange
    ProfileUploadBase sut = new ProfileUploadBase(tempDir.toString());

    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> sut.getProfilePictureAsBase64("../alice"),
        "Username containing '..' must be rejected");
  }

  @Test
  void getProfilePictureAsBase64_rejectsUsernameContainingForwardSlash() {
    // Arrange
    ProfileUploadBase sut = new ProfileUploadBase(tempDir.toString());

    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> sut.getProfilePictureAsBase64("a/b"),
        "Username containing '/' must be rejected");
  }

  @Test
  void getProfilePictureAsBase64_rejectsUsernameContainingBackslash() {
    // Arrange
    ProfileUploadBase sut = new ProfileUploadBase(tempDir.toString());

    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> sut.getProfilePictureAsBase64("a\\b"),
        "Username containing '\\' must be rejected");
  }
}
