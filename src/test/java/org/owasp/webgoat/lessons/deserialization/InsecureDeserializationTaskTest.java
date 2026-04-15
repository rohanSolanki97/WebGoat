package org.owasp.webgoat.lessons.deserialization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class InsecureDeserializationTaskTest {

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty data")
    void testEmptyDataThrowsException() {
        InsecureDeserializationTask task = new InsecureDeserializationTask();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        assertThrows(IllegalArgumentException.class, () -> {
            task.handleTask("", request);
        });
    }

    @Test
    @DisplayName("Should deserialize valid JSON into AllowedData")
    void testValidJsonDeserialization() throws Exception {
        InsecureDeserializationTask task = new InsecureDeserializationTask();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String json = "{\"value\":\"test123\"}";

        String result = task.handleTask(json, request);

        assertEquals("Processed: test123", result);
    }
}