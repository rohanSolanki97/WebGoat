/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography;

import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HashingAssignment implements AssignmentEndpoint {

    @PostMapping("/Hashing/assignment")
    @ResponseBody
    public AttackResult completed(@RequestParam String input) throws NoSuchAlgorithmException {
        // Validate input
        if (!StringUtils.hasText(input)) {
            return failed(this).feedback("hashing.assignment.invalidinput").build();
        }
        if (input.length() > 1024) {
            return failed(this).feedback("hashing.assignment.inputtoolong").build();
        }

        // Use SecureRandom for cryptographically strong salt generation
        SecureRandom secureRandom = new SecureRandom();
        int salt = secureRandom.nextInt();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(Integer.toString(salt).getBytes(StandardCharsets.UTF_8));
        byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));

        if (hashed.length > 0) {
            return success(this).feedback("hashing.assignment.solved").build();
        } else {
            return failed(this).feedback("hashing.assignment.failed").build();
        }
    }
}