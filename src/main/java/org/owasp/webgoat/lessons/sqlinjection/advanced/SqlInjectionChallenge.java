/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.webgoat.container.LessonDataSource;
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
public class SqlInjectionChallenge implements AssignmentEndpoint {

    private final LessonDataSource dataSource;

    @PostMapping("/SqlInjection/attack5")
    @ResponseBody
    public AttackResult completed(@RequestParam String userId) throws Exception {
        // Validate input: non-empty, reasonable length, numeric ID
        if (!StringUtils.hasText(userId)) {
            return failed(this).feedback("sql-injection.challenge.invalidinput").build();
        }
        if (userId.length() > 50) {
            return failed(this).feedback("sql-injection.challenge.inputtoolong").build();
        }
        if (!userId.matches("^[a-zA-Z0-9_-]+$")) {
            return failed(this).feedback("sql-injection.challenge.invalidchars").build();
        }

        try (var connection = dataSource.getConnection()) {
            // Use parameterized query to prevent SQL injection
            String sql = "SELECT * FROM users WHERE userid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return success(this).feedback("sql-injection.challenge.solved").build();
                    } else {
                        return failed(this).feedback("sql-injection.challenge.failed").build();
                    }
                }
            }
        } catch (SQLException e) {
            // Secure logging without sensitive data
            log.error("Database error during SQL Injection challenge: {}", e.getMessage());
            return failed(this).feedback("sql-injection.challenge.dberror").build();
        }
    }
}