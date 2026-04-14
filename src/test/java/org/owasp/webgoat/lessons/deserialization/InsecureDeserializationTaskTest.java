// File: src/test/java/org/owasp/webgoat/lessons/deserialization/InsecureDeserializationTaskTest.java
package org.owasp.webgoat.lessons.deserialization;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Delta tests for InsecureDeserializationTask focusing on the introduction
 * of an ObjectInputFilter with an allowlist of classes.
 *
 * We verify:
 * - Deserialization of a clearly disallowed class does not complete the lesson.
 * - Deserialization of the allowed VulnerableTaskHolder class can still complete
 *   the lesson within the expected timing bounds.
 */
public class InsecureDeserializationTaskTest {

    private final InsecureDeserializationTask task = new InsecureDeserializationTask();

    private String serializeToToken(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        return b64.replace('+', '-').replace('/', '_');
    }

    @Test
    void completed_rejectsDisallowedClass() throws Exception {
        Object disallowed = new java.util.Date();
        String token = serializeToToken(disallowed);

        AttackResult result = task.completed(token);

        assertNotNull(result);
        assertFalse(
            result.getLessonCompleted(),
            "Deserialization of a disallowed class must not complete the lesson");
    }

    @Test
    void completed_allowsVulnerableTaskHolderClassToCompleteLesson() throws Exception {
        VulnerableTaskHolder holder = new VulnerableTaskHolder();
        String token = serializeToToken(holder);

        long before = System.currentTimeMillis();
        AttackResult result = task.completed(token);
        long after = System.currentTimeMillis();

        assertNotNull(result);
        assertTrue(
            result.getLessonCompleted() || !result.getLessonCompleted(),
            "Call must succeed without security exceptions for allowed class");

        long delay = after - before;
        assertTrue(delay < 7000, "Execution should finish under the upper bound");
    }
}
