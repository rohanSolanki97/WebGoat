package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Delta unit tests focusing on the security fixes applied:
 * 1. Null/empty tokens are rejected.
 * 2. SecureObjectInputStream enforces class allowlist.
 * 3. Disallowed classes trigger SecurityException.
 */
public class InsecureDeserializationTaskTest {

    @Test
    @DisplayName("Null token should be rejected")
    void testNullTokenRejected() throws Exception {
        InsecureDeserializationTask task = new InsecureDeserializationTask();
        var result = task.completed(null);
        assertTrue(result.getFeedback().contains("insecure-deserialization.invalidinput"),
                "Feedback should indicate invalid input");
    }

    @Test
    @DisplayName("Empty token should be rejected")
    void testEmptyTokenRejected() throws Exception {
        InsecureDeserializationTask task = new InsecureDeserializationTask();
        var result = task.completed("   ");
        assertTrue(result.getFeedback().contains("insecure-deserialization.invalidinput"),
                "Feedback should indicate invalid input");
    }

    @Test
    @DisplayName("Allowed class should deserialize successfully")
    void testAllowedClassDeserialization() throws Exception {
        VulnerableTaskHolder holder = new VulnerableTaskHolder();
        String token = serializeToBase64(holder);

        InsecureDeserializationTask task = new InsecureDeserializationTask();
        var result = task.completed(token);
        assertTrue(result.getFeedback().isEmpty() || result.getFeedback().contains("success"),
                "Allowed class should result in success feedback");
    }

    @Test
    @DisplayName("Disallowed class should trigger SecurityException feedback")
    void testDisallowedClassDeserialization() throws Exception {
        class MaliciousClass implements Serializable {
            private static final long serialVersionUID = 1L;
        }
        String token = serializeToBase64(new MaliciousClass());

        InsecureDeserializationTask task = new InsecureDeserializationTask();
        var result = task.completed(token);
        assertTrue(result.getFeedback().contains("insecure-deserialization.disallowedclass"),
                "Disallowed class should trigger disallowed class feedback");
    }

    // Helper method to serialize object to Base64 token
    private String serializeToBase64(Object obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
        String b64 = Base64.getEncoder().encodeToString(bos.toByteArray());
        return b64.replace('+', '-').replace('/', '_');
    }
}
