package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Delta unit tests focusing on the change from java.util.Random to java.security.SecureRandom
 * to ensure cryptographically strong randomness is used for secret selection.
 */
public class HashingAssignmentTest {

    private HashingAssignment hashingAssignment;
    private HttpServletRequest requestMock;

    @BeforeEach
    public void setup() {
        hashingAssignment = new HashingAssignment();
        requestMock = Mockito.mock(HttpServletRequest.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Test
    public void testSecureRandomIsUsedForSecretSelection() throws Exception {
        // Arrange
        Set<String> secretsGenerated = new HashSet<>();
        Mockito.when(requestMock.getSession().getAttribute(Mockito.anyString())).thenReturn(null);

        // Act
        for (int i = 0; i < 100; i++) {
            String md5Hash = hashingAssignment.getMd5(requestMock);
            String storedSecret = (String) requestMock.getSession().getAttribute("md5Secret");
            secretsGenerated.add(storedSecret);
        }

        // Assert
        // Ensure multiple different secrets are generated, indicating randomness
        assertTrue(secretsGenerated.size() > 1, "SecureRandom should produce varied secrets");
    }

    @Test
    public void testMd5HashMatchesSelectedSecret() throws Exception {
        // Arrange
        Mockito.when(requestMock.getSession().getAttribute(Mockito.anyString())).thenReturn(null);

        // Act
        String md5Hash = hashingAssignment.getMd5(requestMock);
        String storedSecret = (String) requestMock.getSession().getAttribute("md5Secret");

        // Assert
        String expectedHash = DatatypeConverter.printHexBinary(
                java.security.MessageDigest.getInstance("MD5").digest(storedSecret.getBytes())
        ).toUpperCase();
        assertEquals(expectedHash, md5Hash, "MD5 hash should match the SecureRandom-selected secret");
    }
}
