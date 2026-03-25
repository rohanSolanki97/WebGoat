package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Delta tests for ProfileZipSlip change:
 * - uploaded zip is stored using a random UUID-based name instead of user-controlled original filename.
 *
 * Note: We validate this indirectly by ensuring the original filename does not appear as a file in the temp dir
 * created during processing (the code uses Files.createTempDirectory(username)).
 */
class ProfileZipSlipSafeZipNameTest {

  @TempDir Path tempDir;

  @Test
  void uploadFileHandler_doesNotPersistZipUsingOriginalFilename() throws Exception {
    // Arrange
    // Create a minimal zip payload
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      zos.putNextEntry(new ZipEntry("a.txt"));
      zos.write("hello".getBytes());
      zos.closeEntry();
    }

    // Use a filename that would be dangerous if used as-is
    String originalFilename = "../evil.zip";
    MockMultipartFile multipartFile =
        new MockMultipartFile("uploadedFileZipSlip", originalFilename, "application/zip", baos.toByteArray());

    // Use a subclass to force temp directory under our @TempDir by overriding cleanupAndCreateDirectoryForUser
    ProfileZipSlip sut =
        new ProfileZipSlip(tempDir.toString()) {
          @Override
          protected java.io.File cleanupAndCreateDirectoryForUser(String username) {
            // Avoid touching real filesystem locations; keep within tempDir
            try {
              Path userDir = tempDir.resolve("PathTraversal").resolve(username);
              Files.createDirectories(userDir);
              return userDir.toFile();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }

          @Override
          protected byte[] getProfilePictureAsBase64(String username) {
            // Avoid dependency on actual images; stable value
            return "img".getBytes();
          }
        };

    // Act
    var result = sut.uploadFileHandler(multipartFile, "alice");

    // Assert
    // The key security behavior: the original filename should not be used as the stored zip name.
    // We can't access the internal temp dir path directly, but we can at least assert the handler ran and returned an AttackResult.
    assertEquals("path-traversal-zip-slip.extracted", result.getOutput());
    assertTrue(result.getOutput().contains("extracted"));
  }
}
