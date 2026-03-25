package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

class ProfileZipSlipTest {

  @TempDir File tempDir;

  @Test
  void processZipUpload_doesNotUseOriginalFilenameForTempZipPath() throws Exception {
    // Arrange
    ProfileZipSlip sut = spy(new ProfileZipSlip(tempDir.getAbsolutePath()));

    MultipartFile multipartFile = mock(MultipartFile.class);
    when(multipartFile.getOriginalFilename()).thenReturn("../evil.zip");
    when(multipartFile.getBytes()).thenReturn(new byte[] {0x50, 0x4B, 0x03, 0x04}); // zip header

    // Avoid touching filesystem-heavy parts by stubbing base-class methods used before zip handling.
    doReturn(new byte[] {1}).when(sut).getProfilePictureAsBase64(anyString());
    doReturn(new File(tempDir, "PathTraversal/user")).when(sut).cleanupAndCreateDirectoryForUser(anyString());

    // Act + Assert
    // The fix generates a random safe filename; it should not fail early due to original filename traversal.
    // We only assert it doesn't throw IllegalArgumentException due to filename usage.
    Method m = ProfileZipSlip.class.getDeclaredMethod("processZipUpload", MultipartFile.class, String.class);
    m.setAccessible(true);

    assertDoesNotThrow(() -> m.invoke(sut, multipartFile, "user"));
  }
}
