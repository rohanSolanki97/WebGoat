/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.deserialization;

import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AssignmentHints({
    "insecure-deserialization.hints.1",
    "insecure-deserialization.hints.2",
    "insecure-deserialization.hints.3"
})
public class InsecureDeserializationTask implements AssignmentEndpoint {

    @PostMapping("/InsecureDeserialization/task")
    @ResponseBody
    public AttackResult completed(@RequestParam String token) throws IOException {
        if (token == null || token.isBlank()) {
            return failed(this).feedback("insecure-deserialization.invalidtoken").build();
        }

        String b64token = token.replace('-', '+').replace('_', '/');

        long before;
        long after;
        int delay;

        try {
            byte[] decoded = Base64.getDecoder().decode(b64token);
            try (SafeObjectInputStream ois = new SafeObjectInputStream(new ByteArrayInputStream(decoded))) {
                // Apply JEP 290 serialization filtering — allowlist only VulnerableTaskHolder
                ois.setAllowedClasses(VulnerableTaskHolder.class);

                before = System.currentTimeMillis();
                Object o = ois.readObject();

                if (!(o instanceof VulnerableTaskHolder)) {
                    if (o instanceof String) {
                        return failed(this).feedback("insecure-deserialization.stringobject").build();
                    }
                    return failed(this).feedback("insecure-deserialization.wrongobject").build();
                }
                after = System.currentTimeMillis();
            }
        } catch (InvalidClassException e) {
            log.warn("Invalid class during deserialization: {}", e.getMessage());
            return failed(this).feedback("insecure-deserialization.invalidversion").build();
        } catch (IllegalArgumentException e) {
            log.warn("Illegal argument during token processing: {}", e.getMessage());
            return failed(this).feedback("insecure-deserialization.expired").build();
        } catch (Exception e) {
            log.error("Deserialization error: {}", e.getMessage());
            return failed(this).feedback("insecure-deserialization.invalidversion").build();
        }

        delay = (int) (after - before);
        if (delay > 7000 || delay < 3000) {
            return failed(this).build();
        }
        return success(this).build();
    }

    /**
     * SafeObjectInputStream — wrapper that enforces class allowlist.
     */
    static class SafeObjectInputStream extends ObjectInputStream {
        private Class<?>[] allowedClasses;

        public SafeObjectInputStream(ByteArrayInputStream in) throws IOException {
            super(in);
        }

        public void setAllowedClasses(Class<?>... allowedClasses) {
            this.allowedClasses = allowedClasses;
        }

        @Override
        protected Class<?> resolveClass(java.io.ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            Class<?> clazz = super.resolveClass(desc);
            if (allowedClasses != null) {
                boolean allowed = false;
                for (Class<?> allowedClass : allowedClasses) {
                    if (allowedClass.equals(clazz)) {
                        allowed = true;
                        break;
                    }
                }
                if (!allowed) {
                    throw new InvalidClassException("Unauthorized deserialization attempt: " + clazz.getName());
                }
            }
            return clazz;
        }
    }
}
