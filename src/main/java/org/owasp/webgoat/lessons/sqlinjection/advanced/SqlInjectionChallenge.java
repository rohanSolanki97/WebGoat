/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import lombok.RequiredArgsConstructor;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SqlInjectionChallenge extends AssignmentEndpoint {

  private final LessonDataSource dataSource;

  @PostMapping(path = "/SqlInjectionChallenge/challenge")
  @ResponseBody
  public AttackResult completed(@RequestParam String name) {
    if (name == null || name.trim().isEmpty()) {
      return failed(this).feedback("sql-injection-challenge.name.required").build();
    }

    try (Connection connection = dataSource.getConnection()) {
      PreparedStatement statement;
      // Both branches of the original code were vulnerable to SQL injection.
      // Parameterizing the query fixes the vulnerability for all cases.
      statement = connection.prepareStatement("SELECT * FROM employees WHERE first_name = ?"); // Fixed SQL injection
      statement.setString(1, name); // Set parameter
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return success(this).feedback("sql-injection-challenge.success").build();
      } else {
        return failed(this).feedback("sql-injection-challenge.failed").build();
      }
    } catch (Exception e) {
      return failed(this).feedback("sql-injection-challenge.error").build();
    }
  }
}
