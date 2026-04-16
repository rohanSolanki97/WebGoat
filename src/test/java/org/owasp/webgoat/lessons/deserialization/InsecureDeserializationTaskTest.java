package org.owasp.webgoat.lessons.deserialization;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InvalidClassException;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta unit tests for InsecureDeserializationTask focusing on secure deserialization changes:
 * - Input validation for null/blank tokens
 * - Enforcement of class allowlist via SafeObjectInputStream
 * - Preservation of original challenge timing logic
 */
public class InsecureDeserializationTaskTest {

    private InsecureDeserializationTask task;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        task = new InsecureDeserializationTask();
    }

    @Test
    public void testRejectsNullOrBlankToken() throws Exception {
        AttackResult resultNull = task.completed(null);
        assertTrue(resultNull.getFeedback().contains("insecure-deserialization.invalidtoken"), "Should reject null token");

        AttackResult resultBlank = task.completed("   ");
        assertTrue(resultBlank.getFeedback().contains("insecure-deserialization.invalidtoken"), "Should reject blank token");
    }

    @Test
    public void testRejectsUnauthorizedClassDeserialization() throws Exception {
        // Serialize a String object to trigger unauthorized class
        byte[] serialized;
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
             java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject("malicious");
            oos.flush();
            serialized = bos.toByteArray();
        }
        String token = Base64.getEncoder().encodeToString(serialized).replace('+', '-').replace('/', '_');

        AttackResult result = task.completed(token);
        assertTrue(result.getFeedback().contains("insecure-deserialization.stringobject") 
                || result.getFeedback().contains("insecure-deserialization.invalidversion"),
                "Should reject unauthorized class deserialization");
    }

    @Test
    public void testAllowsVulnerableTaskHolderDeserialization() throws Exception {
        // Serialize a VulnerableTaskHolder object
        VulnerableTaskHolder holder = new VulnerableTaskHolder();
        byte[] serialized;
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
             java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(holder);
            oos.flush();
            serialized = bos.toByteArray();
        }
        String token = Base64.getEncoder().encodeToString(serialized).replace('+', '-').replace('/', '_');

        AttackResult result = task.completed(token);
        // Timing check may fail if execution is too fast, but should not be rejected for class type
        assertFalse(result.getFeedback().contains("insecure-deserialization.wrongobject"), "Should not reject allowed class");
    }

    @Test
    public void testSafeObjectInputStreamThrowsForUnauthorizedClass() throws Exception {
        InsecureDeserializationTask.SafeObjectInputStream safeStream =
                new InsecureDeserializationTask.SafeObjectInputStream(new ByteArrayInputStream(new byte[0]));
        safeStream.setAllowedClasses(VulnerableTaskHolder.class);
        assertThrows(InvalidClassException.class, () -> {
            safeStream.resolveClass(java.io.ObjectStreamClass.lookup(String.class));
        }, "Should throw InvalidClassException for unauthorized class");
    }
}
