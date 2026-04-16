package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.WebGoatUser;

/**
 * Delta tests for BlindSendFileAssignment focusing on the secure path construction changes:
 * - username is sanitized before being used in a path
 * - created secret file path is enforced to stay within the intended directory
 */
public class BlindSendFileAssignmentTest {

  private Path tempWebGoatHome;

  private BlindSendFileAssignment createAssignment(String username) throws Exception {
    tempWebGoatHome = Files.createTempDirectory("webgoat-home-");
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment =
        new BlindSendFileAssignment(tempWebGoatHome.toString(), commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    Mockito.when(user.getUsername()).thenReturn(username);

    // initialization triggers secret file creation via createSecretFileWithRandomContents
    assignment.initialize(user);
    return assignment;
  }

  @AfterEach
  void cleanup() throws Exception {
    if (tempWebGoatHome != null) {
      // best-effort cleanup
      Files.walk(tempWebGoatHome)
          .sorted((a, b) -> b.compareTo(a))
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  @Test
  void initialize_withNormalUsername_createsSecretInsideUserDirectory() throws Exception {
    // Arrange
    String username = "alice";

    // Act
    createAssignment(username);

    // Assert
    File userDir = tempWebGoatHome.resolve("XXE").resolve(username).toFile();
    File secretFile = new File(userDir, "secret.txt");
    assertTrue(
        secretFile.exists(),
        "Secret file should be created inside a directory derived from the sanitized username");
  }

  @Test
  void initialize_withPathTraversalUsername_doesNotEscapeBaseDirectory() throws Exception {
    // Arrange
    String maliciousUsername = "../evilUser";

    // Act
    createAssignment(maliciousUsername);

    // Assert
    // Because FilenameUtils.getName is used, the actual directory should be just "evilUser"
    File expectedUserDir = tempWebGoatHome.resolve("XXE").resolve("evilUser").toFile();
    File secretFile = new File(expectedUserDir, "secret.txt");
    assertTrue(
        secretFile.exists(),
        "Secret file must be created in a sanitized directory name, not using raw ../ segments");

    // Additionally check that no unintended directory was created above the base
    File parent = tempWebGoatHome.getParent().toFile();
    File unintendedDir = new File(parent, "evilUser");
    if (unintendedDir.exists()) {
      fail("Path traversal should not create directories outside the configured base directory");
    }
  }
}
