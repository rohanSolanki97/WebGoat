package org.owasp.webgoat.lessons.xxe;

import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for BlindSendFileAssignment focusing on the secured path
 * construction in createSecretFileWithRandomContents, which now sanitizes the
 * username with FilenameUtils.getName to prevent path traversal.
 */
public class BlindSendFileAssignmentTest {

  @Test
  @DisplayName("createSecretFileWithRandomContents sanitizes username in directory path")
  void createSecretFileWithRandomContents_sanitizesUsername() throws Exception {
    String baseDir = Files.createTempDirectory("webgoat-xxe-").toFile().getAbsolutePath();
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);

    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    when(user.getUsername()).thenReturn("../evilUser");

    // Use reflection to invoke the private method so we can verify the directory path.
    var method =
        BlindSendFileAssignment.class.getDeclaredMethod(
            "createSecretFileWithRandomContents", WebGoatUser.class);
    method.setAccessible(true);
    method.invoke(assignment, user);

    // Access internal map to find stored content and infer the created directory.
    Field mapField =
        BlindSendFileAssignment.class.getDeclaredField("userToFileContents");
    mapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    var userToFileContents =
        (java.util.Map<WebGoatUser, String>) mapField.get(assignment);

    // Ensure an entry exists for our mocked user, implying the file was written.
    org.junit.jupiter.api.Assertions.assertTrue(
        userToFileContents.containsKey(user), "Secret content must be stored for the user");

    // The directory must be under baseDir/XXE/<sanitizedName>, not honoring '../'.
    File xxeDir = new File(baseDir, "/XXE/evilUser");
    org.junit.jupiter.api.Assertions.assertTrue(
        xxeDir.exists() && xxeDir.isDirectory(),
        "Sanitized directory 'evilUser' under /XXE must exist, without path traversal");

    File secretFile = new File(xxeDir, "secret.txt");
    org.junit.jupiter.api.Assertions.assertTrue(
        secretFile.exists(), "Secret file must be created under sanitized directory");
  }
}
