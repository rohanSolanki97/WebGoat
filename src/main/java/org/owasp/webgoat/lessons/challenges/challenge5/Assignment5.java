/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
lombok.RequiredArgsConstructor;
lombok.extern.slf4j.Slf4j;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.lessons.challenges.Flags;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class Assignment5 implements AssignmentEndpoint {

    private final LessonDataSource dataSource;
    private final Flags flags;

    // Strict username pattern: only letters, numbers, underscores, min 3 chars
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,30}$");

    @PostMapping("/challenge/5")
    @ResponseBody
    public AttackResult login(
            @RequestParam String username_login,
            @RequestParam String password_login) throws Exception {

        // Null/empty validation
        if (!StringUtils.hasText(username_login) || !StringUtils.hasText(password_login)) {
            return failed(this).feedback("required4").build();
        }

        // Username format validation
        if (!USERNAME_PATTERN.matcher(username_login).matches()) {
            return failed(this).feedback("invalid.username.format").build();
        }

        // Business logic check
        if (!"Larry".equals(username_login)) {
            return failed(this).feedback("user.not.larry").feedbackArgs(username_login).build();
        }

        try (var connection = dataSource.getConnection()) {
            // Use parameterized query to prevent SQL injection
            String sql = "SELECT password FROM challenge_users WHERE userid = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username_login);
                statement.setString(2, password_login);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return success(this)
                                .feedback("challenge.solved")
                                .feedbackArgs(flags.getFlag(5))
                                .build();
                    } else {
                        return failed(this).feedback("challenge.close").build();
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Database error during login attempt", e);
            return failed(this).feedback("error.database").build();
        }
    }
}