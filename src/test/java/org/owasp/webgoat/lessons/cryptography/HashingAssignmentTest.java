// File: src/test/java/org/owasp/webgoat/lessons/cryptography/HashingAssignmentTest.java
package org.owasp.webgoat.lessons.cryptography;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.assignments.AttackResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Delta tests for HashingAssignment focusing on the change from Random to SecureRandom.
 *
 * We verify:
 * - The set of possible secrets is unchanged (still one of SECRETS).
 * - The hashing/session behavior is preserved.
 */
public class HashingAssignmentTest {

    private HashingAssignment hashingAssignment;
    private HttpServletRequest request;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        hashingAssignment = new HashingAssignment();
        request = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    void getMd5_firstCallStoresSecretAndHashInSession() throws NoSuchAlgorithmException {
        when(session.getAttribute("md5Hash")).thenReturn(null);

        String returnedHash = hashingAssignment.getMd5(request);

        assertNotNull(returnedHash, "MD5 hash should be generated on first call");
        verify(session).setAttribute(eq("md5Hash"), anyString());
        ArgumentCaptor<String> secretCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).setAttribute(eq("md5Secret"), secretCaptor.capture());

        String storedSecret = secretCaptor.getValue();
        assertNotNull(storedSecret);
        assertTrue(
            java.util.Arrays.asList(HashingAssignment.SECRETS).contains(storedSecret),
            "Secret must be one of the predefined SECRETS");
    }

    @Test
    void getSha256_firstCallStoresSecretAndHashInSession() throws NoSuchAlgorithmException {
        when(session.getAttribute("sha256")).thenReturn(null);

        String returnedHash = hashingAssignment.getSha256(request);

        assertNotNull(returnedHash, "SHA-256 hash should be generated on first call");
        verify(session).setAttribute(eq("sha256Hash"), anyString());
        ArgumentCaptor<String> secretCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).setAttribute(eq("sha256Secret"), secretCaptor.capture());

        String storedSecret = secretCaptor.getValue();
        assertNotNull(storedSecret);
        assertTrue(
            java.util.Arrays.asList(HashingAssignment.SECRETS).contains(storedSecret),
            "Secret must be one of the predefined SECRETS");
    }

    @Test
    void completed_succeedsWhenCorrectSecretsAreProvided() throws NoSuchAlgorithmException {
        when(session.getAttribute("md5Hash")).thenReturn(null);
        when(session.getAttribute("sha256")).thenReturn(null);

        hashingAssignment.getMd5(request);
        hashingAssignment.getSha256(request);

        // Capture what secrets were stored by the fixed implementation
        ArgumentCaptor<String> md5SecretCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).setAttribute(eq("md5Secret"), md5SecretCaptor.capture());
        String md5Secret = md5SecretCaptor.getValue();

        ArgumentCaptor<String> shaSecretCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).setAttribute(eq("sha256Secret"), shaSecretCaptor.capture());
        String shaSecret = shaSecretCaptor.getValue();

        when(session.getAttribute("md5Secret")).thenReturn(md5Secret);
        when(session.getAttribute("sha256Secret")).thenReturn(shaSecret);

        AttackResult result = hashingAssignment.completed(request, md5Secret, shaSecret);

        assertNotNull(result);
        assertTrue(result.getLessonCompleted(), "Supplying the correct secrets should complete the lesson");
    }
}
