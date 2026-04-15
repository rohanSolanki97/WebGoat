package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Delta unit tests focusing on the change from java.util.Random to java.security.SecureRandom
 * in HashingAssignment. These tests ensure that the secret selection is now using SecureRandom
 * and that the functional behavior remains correct.
 */
public class HashingAssignmentTest {

    private HashingAssignment hashingAssignment;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;

    @BeforeEach
    void setUp() {
        hashingAssignment = new HashingAssignment();
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);
    }

    @Test
    void testSecretSelectionUsesSecureRandom() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("md5Hash")).thenReturn(null);

        // Act
        hashingAssignment.getMd5(requestMock);

        // Assert
        // Verify that the secret stored in session is one from SECRETS
        verify(sessionMock).setAttribute(eq("md5Secret"), argThat(secret ->
                Arrays.asList(HashingAssignment.SECRETS).contains(secret)
        ));
    }

    @Test
    void testSecureRandomProducesVariedSecrets() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("md5Hash")).thenReturn(null);

        Set<String> seenSecrets = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            // Reset mocks for each iteration
            sessionMock = mock(HttpSession.class);
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("md5Hash")).thenReturn(null);

            hashingAssignment.getMd5(requestMock);
            // Capture the secret
            verify(sessionMock).setAttribute(eq("md5Secret"), argThat(secret -> {
                seenSecrets.add(secret);
                return true;
            }));
        }

        // Assert that more than one unique secret was produced, indicating randomness
        assertTrue(seenSecrets.size() > 1, "SecureRandom should produce varied secrets over multiple runs");
    }

    @Test
    void testSecureRandomInstanceIsStaticAndReused() {
        SecureRandom instance1 = getSecureRandomInstance();
        SecureRandom instance2 = getSecureRandomInstance();
        assertSame(instance1, instance2, "SecureRandomHolder.INSTANCE should be reused across calls");
    }

    private SecureRandom getSecureRandomInstance() {
        try {
            var holderClass = Class.forName("org.owasp.webgoat.lessons.cryptography.HashingAssignment$SecureRandomHolder");
            var field = holderClass.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            return (SecureRandom) field.get(null);
        } catch (Exception e) {
            fail("Unable to access SecureRandomHolder.INSTANCE: " + e.getMessage());
            return null;
        }
    }
}
