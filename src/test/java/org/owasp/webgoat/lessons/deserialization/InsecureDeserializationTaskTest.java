package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

/*
 * Resolved test path:
 * src/test/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTaskTest.java
 */
public class InsecureDeserializationTaskTest {

  @Test
  @DisplayName("completed does not succeed when deserializing a non-whitelisted type (ObjectInputFilter enforcement)")
  void completed_rejectsNonWhitelistedType() throws IOException {
    // Arrange
    InsecureDeserializationTask task = new InsecureDeserializationTask();

    // Serialize a type that is NOT part of the ObjectInputFilter whitelist
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(new java.util.Date()); // not whitelisted
    }
    String token =
        Base64.getEncoder()
            .encodeToString(baos.toByteArray())
            .replace('+', '-')
            .replace('/', '_');

    // Act
    AttackResult result = task.completed(token);

    // Assert
    // If the filter is working, this must not be treated as a valid VulnerableTaskHolder
    // and must not complete the lesson.
    assertFalse(
        result.isLessonCompleted(),
        "Deserialization of a non-whitelisted type must not complete the lesson");
  }
}
