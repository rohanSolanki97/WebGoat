package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Delta unit tests focusing on verifying that SecureRandom is used instead of Random
 * and that functional behavior remains correct after the fix.
 *
 * This test assumes the HashingAssignment code has been refactored to allow injection
 * of the RNG for testing purposes via a setter or constructor.
 */
public class HashingAssignmentTest {

    private HashingAssignment hashingAssignment;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void setUp() {
        hashingAssignment = new HashingAssignment();
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);
    }

    @Test
    public void testGetMd5_UsesInjectedSecureRandomAndStoresSecret() throws NoSuchAlgorithmException {
        // Arrange: Inject a SecureRandom mock to verify usage
        SecureRandom secureRandomMock = mock(SecureRandom.class);
        when(secureRandomMock.nextInt(HashingAssignment.SECRETS.length)).thenReturn(2); // deterministic secret
        hashingAssignment.setSecureRandom(secureRandomMock);
        when(sessionMock.getAttribute("md5Hash")).thenReturn(null);

        // Act
        String resultHash = hashingAssignment.getMd5(requestMock);

        // Assert
        assertNotNull(resultHash, "MD5 hash should not be null");
        verify(secureRandomMock).nextInt(HashingAssignment.SECRETS.length);
        ArgumentCaptor<String> secretCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionMock).setAttribute(eq("md5Secret"), secretCaptor.capture());
        assertEquals(HashingAssignment.SECRETS[2], secretCaptor.getValue(),
                "Secret should match the deterministic value from SecureRandom mock");
    }

    @Test
    public void testGetSha256_UsesInjectedSecureRandomAndStoresSecret() throws NoSuchAlgorithmException {
        // Arrange: Inject a SecureRandom mock to verify usage
        SecureRandom secureRandomMock = mock(SecureRandom.class);
        when(secureRandomMock.nextInt(HashingAssignment.SECRETS.length)).thenReturn(4); // deterministic secret
        hashingAssignment.setSecureRandom(secureRandomMock);
        when(sessionMock.getAttribute("sha256")).thenReturn(null);

        // Act
        String resultHash = hashingAssignment.getSha256(requestMock);

        // Assert
        assertNotNull(resultHash, "SHA-256 hash should not be null");
        verify(secureRandomMock).nextInt(HashingAssignment.SECRETS.length);
        ArgumentCaptor<String> secretCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionMock).setAttribute(eq("sha256Secret"), secretCaptor.capture());
        assertEquals(HashingAssignment.SECRETS[4], secretCaptor.getValue(),
                "Secret should match the deterministic value from SecureRandom mock");
    }

    @Test
    public void testSecretsArrayContainsExpectedValues() {
        assertTrue(Arrays.asList(HashingAssignment.SECRETS).contains("secret"));
        assertTrue(Arrays.asList(HashingAssignment.SECRETS).contains("admin"));
    }
}
