package org.owasp.webgoat.lessons.pathtraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

class ProfileUploadBaseTest {

  @Test
  void execute_usesBasenameOfFullNameAndPreventsPathTraversalOutsideUserDirectory()
      throws Exception {
    String baseDir = "target/path-traversal-home";
    String username = "alice";
    String dangerousName = "../evil.txt";

    ProfileUploadBase base = new ProfileUploadBase(baseDir);

    MultipartFile file = Mockito.mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getBytes()).thenReturn("data".getBytes());

    base.execute(file, dangerousName, username);

    File userDir = new File(baseDir, "/PathTraversal/" + username);
    assertTrue(userDir.exists(), "User upload directory should exist");

    File[] uploaded = userDir.listFiles();
    assertTrue(uploaded != null && uploaded.length == 1, "Exactly one uploaded file is expected");

    File uploadedFile = uploaded[0];

    // The uploaded file name must be the sanitized basename only
    assertEquals("evil.txt", uploadedFile.getName(), "FullName must be normalized to basename");

    // The canonical path of the uploaded file must remain within the user directory
    String userCanonical = userDir.getCanonicalPath();
    String uploadedCanonical = uploadedFile.getCanonicalPath();
    assertTrue(
        uploadedCanonical.startsWith(userCanonical),
        "Uploaded file must remain within the user-specific directory");

    // Ensure file content actually written
    assertTrue(Files.size(uploadedFile.toPath()) > 0);
  }
}
