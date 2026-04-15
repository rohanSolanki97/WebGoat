package org.owasp.webgoat.lessons.deserialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InsecureDeserializationTask {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/deserialization/task")
    public String handleTask(@RequestParam("data") String data, HttpServletRequest request) throws IOException {
        // Validate JSON format
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty");
        }

        // Safe deserialization into allowed type
        AllowedData obj = objectMapper.readValue(data, AllowedData.class);

        // Process safely
        return "Processed: " + obj.getValue();
    }

    public static class AllowedData {
        private String value;
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}