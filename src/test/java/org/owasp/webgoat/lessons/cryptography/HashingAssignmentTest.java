package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Delta unit tests focusing on the change from java.util.Random to java.security.SecureRandom
 * in HashingAssignment. These tests ensure SecureRandom is used and functional behavior is preserved.
 */
public class HashingAssignmentTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpSession mockSession;

    private HashingAssignment hashingAssignment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hashingAssignment = new HashingAssignment();
        when(mockRequest.getSession()).thenReturn(mockSession);
    }

    @Test
    public void testSecureRandomIsUsedForSecretSelection() throws NoSuchAlgorithmException {
        when(mockSession.getAttribute("md5Hash")).thenReturn(null);
        String md5Hash = hashingAssignment.getMd5(mockRequest);
        assertNotNull(md5Hash, "MD5 hash should not be null");
        assertTrue(HashingAssignment.SecureRandomHolder.INSTANCE instanceof SecureRandom,
                "SecureRandomHolder.INSTANCE should be an instance of SecureRandom");
    }

    @Test
    public void testSecretSelectionProducesValidHash() throws NoSuchAlgorithmException {
        when(mockSession.getAttribute("sha256")).thenReturn(null);
        String sha256Hash = hashingAssignment.getSha256(mockRequest);
        assertNotNull(sha256Hash, "SHA-256 hash should not be null");
        assertEquals(64, sha256Hash.length(), "SHA-256 hash should be 64 hex characters");
        assertTrue(sha256Hash.matches("^[A-F0-9]+$"), "SHA-256 hash should be uppercase hex");
    }

    @Test
    public void testGetHashMethodProducesExpectedHash() throws NoSuchAlgorithmException {
        String secret = "testSecret";
        String expectedSha256 = DatatypeConverter.printHexBinary(
                java.security.MessageDigest.getInstance("SHA-256").digest(secret.getBytes())
        ).toUpperCase();
        String actualSha256 = HashingAssignment.getHash(secret, "SHA-256");
        assertEquals(expectedSha256, actualSha256, "getHash should produce correct SHA-256 hash");
    }
}
