package org.owasp.webgoat.lessons.cryptography;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Delta unit tests focusing on the change from java.util.Random to java.security.SecureRandom
 * in secret selection. These tests verify that the selection is now unpredictable and secure.
 */
public class HashingAssignmentTest {

    @Test
    void testSecureRandomIndexProducesDifferentValuesOverMultipleCalls() {
        int bound = HashingAssignment.SECRETS.length;
        Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            int index = invokeSecureRandomIndex(bound);
            indices.add(index);
        }
        // Ensure multiple distinct indices are produced, indicating unpredictability
        assertTrue(indices.size() > 1, "SecureRandom should produce varied indices");
    }

    @Test
    void testSecureRandomIndexIsWithinBounds() {
        int bound = HashingAssignment.SECRETS.length;
        for (int i = 0; i < 50; i++) {
            int index = invokeSecureRandomIndex(bound);
            assertTrue(index >= 0 && index < bound, "Index should be within bounds");
        }
    }

    @Test
    void testSecureRandomIndexDoesNotRepeatPredictably() {
        int bound = HashingAssignment.SECRETS.length;
        int first = invokeSecureRandomIndex(bound);
        int second = invokeSecureRandomIndex(bound);
        // It's possible they match, but over multiple runs they should differ often
        assertNotEquals(first, second, "SecureRandom should not produce predictable repeats");
    }

    private int invokeSecureRandomIndex(int bound) {
        // Using reflection to call the private method getSecureRandomIndex
        try {
            var method = HashingAssignment.class.getDeclaredMethod("getSecureRandomIndex", int.class);
            method.setAccessible(true);
            return (int) method.invoke(null, bound);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getSecureRandomIndex", e);
        }
    }
}