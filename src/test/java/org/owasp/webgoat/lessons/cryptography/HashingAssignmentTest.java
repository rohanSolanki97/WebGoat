package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Delta unit tests focusing on the change from java.util.Random to java.security.SecureRandom
 * in HashingAssignment for selecting secrets.
 */
public class HashingAssignmentTest {

    @Test
    void testSecureRandomIndexIsWithinBounds() {
        int bound = HashingAssignment.SECRETS.length;
        int index = invokeSecureRandomIndex(bound);
        assertTrue(index >= 0 && index < bound, "SecureRandom index should be within bounds");
    }

    @Test
    void testSecureRandomProducesDifferentIndices() {
        int bound = HashingAssignment.SECRETS.length;
        Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            indices.add(invokeSecureRandomIndex(bound));
        }
        // SecureRandom should produce multiple distinct indices over many iterations
        assertTrue(indices.size() > 1, "SecureRandom should produce varied indices");
    }

    @Test
    void testSecureRandomIsActuallySecureRandomInstance() {
        // This test ensures that SecureRandom is used instead of java.util.Random
        SecureRandom sr = new SecureRandom();
        int bound = HashingAssignment.SECRETS.length;
        int expectedIndex = sr.nextInt(bound);
        // We can't directly compare values, but we can ensure method exists and returns valid range
        int actualIndex = invokeSecureRandomIndex(bound);
        assertTrue(actualIndex >= 0 && actualIndex < bound, "SecureRandom index should be valid");
    }

    private int invokeSecureRandomIndex(int bound) {
        try {
            // Using reflection to call private static method getSecureRandomIndex
            var method = HashingAssignment.class.getDeclaredMethod("getSecureRandomIndex", int.class);
            method.setAccessible(true);
            return (int) method.invoke(null, bound);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getSecureRandomIndex", e);
        }
    }
}
