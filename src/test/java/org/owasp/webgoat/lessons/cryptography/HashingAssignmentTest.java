package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Delta unit tests focusing on the security fixes applied:
 * 1. Use of SecureRandom instead of Random for secret selection.
 * 2. Replacement of MD5 with SHA-256 in getMd5() method.
 */
public class HashingAssignmentTest {

    @Test
    @DisplayName("getMd5 should use SecureRandom and SHA-256 hashing")
    void testGetMd5UsesSecureRandomAndSHA256() throws NoSuchAlgorithmException {
        // Arrange
        HashingAssignment assignment = new HashingAssignment();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("md5Hash")).thenReturn(null);

        // Act
        String hash = assignment.getMd5(request);

        // Assert
        assertNotNull(hash, "Hash should not be null");
        assertEquals(64, hash.length(), "SHA-256 hash should be 64 hex characters");
        verify(session).setAttribute(eq("md5Hash"), anyString());
        verify(session).setAttribute(eq("md5Secret"), anyString());
    }

    @Test
    @DisplayName("SecureRandom index generation should be within bounds")
    void testSecureRandomIndexWithinBounds() {
        int bound = HashingAssignment.SECRETS.length;
        int index = invokeSecureRandomIndex(bound);
        assertTrue(index >= 0 && index < bound, "Index should be within valid range");
    }

    // Helper to invoke private method getSecureRandomIndex via reflection
    private int invokeSecureRandomIndex(int bound) {
        try {
            var method = HashingAssignment.class.getDeclaredMethod("getSecureRandomIndex", int.class);
            method.setAccessible(true);
            return (int) method.invoke(null, bound);
        } catch (Exception e) {
            fail("Failed to invoke getSecureRandomIndex: " + e.getMessage());
            return -1;
        }
    }
}
