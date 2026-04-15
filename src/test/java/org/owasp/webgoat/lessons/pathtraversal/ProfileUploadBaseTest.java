package org.owasp.webgoat.lessons.pathtraversal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.Mockito;

/**
 * Delta test for ProfileUploadBase (BATCH-009)
 * Path: src/test/java/org/owasp/webgoat/lessons/pathtraversal/ProfileUploadBaseTest.java
 *
 * Focus:
 *  - execute(): use of FilenameUtils.getName(fullName) prevents directory components in filename.
 *  - cleanupAndCreateDirectoryForUser() and getProfilePictureAsBase64(): username is sanitized
 *    before being used in directory names.
 */
class ProfileUploadBaseTest {

  @Test
  void execute_shouldStripDirectoryComponentsFromFilename() throws Exception {
    // Arrange
    File tmp = Files.createTempDirectory("webgoat-home-pt-").toFile();
    String baseDir = tmp.getAbsolutePath();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
    Mockito.when(multipartFile.isEmpty()).thenReturn(false);
    Mockito.when(multipartFile.getBytes()).thenReturn("data".getBytes());

    String maliciousName = "../evil/../profile.png";
    String username = "user";

    // Act
    base.execute(multipartFile, maliciousName, username);

    // Assert: file should exist under /PathTraversal/{sanitizedUsername} with sanitized filename
    String expectedFileName =
        org.apache.commons.io.FilenameUtils.getName(maliciousName);
    String sanitizedUsername = username.replaceAll("[^a-zA-Z0-9-_.]", "");
    File expectedDir = new File(baseDir, "/PathTraversal/" + sanitizedUsername);
    File uploaded = new File(expectedDir, expectedFileName);

    assertThat(uploaded.isFile()).isTrue();

    // Ensure directory traversal from fullName was not honored
    File dangerous = new File(baseDir, "/PathTraversal/" + maliciousName);
    assertThat(dangerous.getCanonicalPath()).isNotEqualTo(uploaded.getCanonicalPath());
  }

  @Test
  void getProfilePicture_shouldUseSanitizedUsernameDirectory() throws Exception {
    // Arrange
    File tmp = Files.createTempDirectory("webgoat-home-pt2-").toFile();
    String baseDir = tmp.getAbsolutePath();
    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    String maliciousUsername = "../eviluser";
    String sanitized = maliciousUsername.replaceAll("[^a-zA-Z0-9-_.]", "");

    File profileDir = new File(baseDir, "/PathTraversal/" + sanitized);
    Files.createDirectories(profileDir.toPath());
    File img = new File(profileDir, "pic.jpg");
    Files.write(img.toPath(), "img".getBytes());

    // Act
    ResponseEntity<?> response = base.getProfilePicture(maliciousUsername);

    // Assert
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    Object body = response.getBody();
    assertThat(body).isInstanceOf(byte[].class);
  }
}
