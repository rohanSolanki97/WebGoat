package org.owasp.webgoat.lessons.xxe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.users.WebGoatUser;

class BlindSendFileAssignmentTest {

  @Test
  void createSecretFileWithRandomContents_usesSanitizedUsernameBasenameAndPreventsTraversal()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    String baseDir = "target/webgoat-home";
    CommentsCache commentsCache = Mockito.mock(CommentsCache.class);
    BlindSendFileAssignment assignment = new BlindSendFileAssignment(baseDir, commentsCache);

    WebGoatUser user = Mockito.mock(WebGoatUser.class);
    // Username contains traversal and multiple path components
    Mockito.when(user.getUsername()).thenReturn("user/../evil/../../other");

    Method m =
        BlindSendFileAssignment.class.getDeclaredMethod(
            "createSecretFileWithRandomContents", WebGoatUser.class);
    m.setAccessible(true);
    m.invoke(assignment, user);

    File xxeRoot = new File(baseDir, "/XXE");
    assertTrue(xxeRoot.exists(), "XXE base directory must exist");

    File[] dirs = xxeRoot.listFiles();
    assertTrue(dirs != null && dirs.length == 1, "Exactly one user directory is expected");

    File createdDir = dirs[0];

    // The directory name must be the sanitized basename of the username (FilenameUtils.getName)
    // which for "user/../evil/../../other" resolves to "other"
    assertEquals("other", createdDir.getName(), "Username used for directory must be sanitized");

    // And the resulting directory must remain under the XXE base directory
    String xxeCanonical = xxeRoot.getCanonicalPath();
    String createdCanonical = createdDir.getCanonicalPath();
    assertTrue(
        createdCanonical.startsWith(xxeCanonical),
        "Created directory must remain within the XXE base directory");
  }
}
