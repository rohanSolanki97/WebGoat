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
import java.io.ObjectInputFilter; // Added import for ObjectInputFilter
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AssignmentHints({
  "insecure-deserialization.hints.1",
  "insecure-deserialization.hints.2",
  "insecure-deserialization.hints.3"
})
public class InsecureDeserializationTask implements AssignmentEndpoint {

  @PostMapping("/InsecureDeserialization/task")
  @ResponseBody
  public AttackResult completed(@RequestParam String token) throws IOException {
    String b64token;
    long before;
    long after;
    int delay;

    b64token = token.replace('-', '+').replace('_', '/');

    try (ObjectInputStream ois =
        new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(b64token)))) {
      // Apply a serialization filter to restrict allowed classes (JEP 290)
      ois.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
          "org.dummy.insecure.framework.VulnerableTaskHolder;java.lang.String;!*")); // Fixed insecure deserialization
      before = System.currentTimeMillis();
      Object o = ois.readObject();
      if (!(o instanceof VulnerableTaskHolder)) {
        if (o instanceof String) {
          return failed(this)
              .feedback("insecure-deserialization.string")
              .feedbackArgs(o)
              .build();
        }
        return failed(this).feedback("insecure-deserialization.wrong.class").build();
      }
      // ... (rest of the method remains unchanged)
      VulnerableTaskHolder holder = (VulnerableTaskHolder) o;
      delay = holder.getDelay();
      after = System.currentTimeMillis();

      if (after - before > delay) {
        return success(this).feedback("insecure-deserialization.success").build();
      } else {
        return failed(this).feedback("insecure-deserialization.failed").build();
      }
    } catch (InvalidClassException e) {
      return failed(this).feedback("insecure-deserialization.invalid.class").build();
    } catch (ClassNotFoundException e) {
      return failed(this).feedback("insecure-deserialization.class.not.found").build();
    }
  }
}
