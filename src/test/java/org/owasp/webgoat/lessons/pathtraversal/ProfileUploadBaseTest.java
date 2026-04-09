package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.users.WebGoatUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delta tests for ProfileUploadBase focusing on path-traversal protection using
 * Path.normalize() and startsWith(userDir).
 */
public class ProfileUploadBaseTest {

  @Test
  void upload_shouldStoreFileUnderUserDirectoryForNormalRelativePath() throws Exception {
    // Arrange
    Path baseDir = Files.createTempDirectory("webgoat-profile-upload-");
    ProfileUploadBase controller = new ProfileUploadBase(baseDir.toString());

    MultipartFile file =
        new MockMultipartFile("profile", "avatar.png", "image/png", "img".getBytes());
    WebGoatUser user = new WebGoatUser("bob", "bob", "ROLE_USER");

    // Act
    controller.upload(file, "avatar.png", user);

    // Assert: file should exist under <base>/profile-upload/bob/avatar.png
    Path userDir = baseDir.resolve("profile-upload").resolve("bob").normalize();
    Path storedFile = userDir.resolve("avatar.png");
    assertTrue(
        Files.exists(storedFile),
        "File uploaded with a simple filename must be stored within the user's directory");
  }

  @Test
  void upload_shouldRejectPathTraversalOutsideUserDirectory() throws Exception {
    // Arrange
    Path baseDir = Files.createTempDirectory("webgoat-profile-upload-");
    ProfileUploadBase controller = new ProfileUploadBase(baseDir.toString());

    MultipartFile file =
        new MockMultipartFile("profile", "avatar.png", "image/png", "img".getBytes());
    WebGoatUser user = new WebGoatUser("alice", "alice", "ROLE_USER");

    // Act
    // Attempt to escape userDir using .. segments
    var result = controller.upload(file, "../evil/escape.png", user);

    // Assert: lesson must not be completed and file must not be created outside the user dir
    assertFalse(
        result.getLessonCompleted(),
        "Upload with traversal path must not be considered a successful lesson completion");

    Path evilTarget =
        baseDir.resolve("profile-upload").getParent().resolve("evil").resolve("escape.png");
    assertFalse(
        Files.exists(evilTarget),
        "Traversal using '../evil/escape.png' must not create a file outside the user directory");
  }
}
