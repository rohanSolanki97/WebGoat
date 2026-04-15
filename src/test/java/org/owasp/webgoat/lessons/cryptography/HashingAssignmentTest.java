package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Delta unit tests for HashingAssignment focusing on the change from java.util.Random to
 * java.security.SecureRandom to ensure unpredictability of secret selection.
 */
public class HashingAssignmentTest {

    @Test
    @DisplayName("SecureRandomHolder should use SecureRandom instance")
    void testSecureRandomHolderUsesSecureRandom() {
        // Arrange & Act
        SecureRandom sr = HashingAssignment.SecureRandomHolder.INSTANCE;

        // Assert
        assertTrue(sr instanceof SecureRandom, "INSTANCE should be of type SecureRandom");
    }

    @RepeatedTest(5)
    @DisplayName("SecureRandomHolder should produce varied outputs across invocations")
    void testSecureRandomProducesDifferentValues() {
        // Arrange
        Set<Integer> results = new HashSet<>();
        int bound = HashingAssignment.SECRETS.length;

        // Act
        for (int i = 0; i < 10; i++) {
            results.add(HashingAssignment.SecureRandomHolder.INSTANCE.nextInt(bound));
        }

        // Assert
        assertTrue(results.size() > 1, "SecureRandom should produce varied outputs");
    }

    @Test
    @DisplayName("SecureRandomHolder should not produce predictable sequence")
    void testSecureRandomIsNotPredictable() {
        // Arrange
        int bound = HashingAssignment.SECRETS.length;
        int first = HashingAssignment.SecureRandomHolder.INSTANCE.nextInt(bound);
        int second = HashingAssignment.SecureRandomHolder.INSTANCE.nextInt(bound);

        // Assert
        assertNotEquals(first, second, "Two consecutive calls should not always be equal");
    }
}
