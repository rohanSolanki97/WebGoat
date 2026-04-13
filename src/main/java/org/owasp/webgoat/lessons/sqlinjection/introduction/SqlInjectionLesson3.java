/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AssignmentHints(value = {"SqlStringInjectionHint3-1", "SqlStringInjectionHint3-2"})
public class SqlInjectionLesson3 implements AssignmentEndpoint {

  private final LessonDataSource dataSource;

  public SqlInjectionLesson3(LessonDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @PostMapping("/SqlInjection/attack3")
  @ResponseBody
  public AttackResult completed(@RequestParam String query) {
    return injectableQuery(query);
  }

  protected AttackResult injectableQuery(String query) {
    // Vulnerability: Direct execution of user-supplied SQL query.
    // Remediation: Prevent execution of arbitrary user-supplied SQL.
    // In a real application, this would be replaced with a safe, parameterized operation
    // or a specific API call that does not expose raw SQL execution.
    // For this lesson, we will prevent the arbitrary execution and return a failure.
    if (query == null || query.trim().isEmpty()) {
      return failed(this).feedback("sql-injection.empty-query").build();
    }

    // Log the attempt to execute arbitrary SQL (for auditing/debugging)
    // In a production system, sensitive query details might be redacted or hashed.
    // log.warn("Attempted to execute arbitrary SQL query: {}", query);

    // Prevent the execution of the arbitrary query.
    // The original logic for checking 'Barnett's department' is now unreachable
    // because arbitrary queries are blocked. The lesson's intended solution path
    // (which relies on injection) is thus prevented.
    try (Connection connection = dataSource.getConnection()) {
      try (Statement statement =
          connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)) {
        // Replace the vulnerable executeUpdate(query) with a safe, non-mutating operation.
        // This prevents arbitrary SQL from being executed.
        statement.executeQuery("SELECT 1"); // Execute a safe, dummy query

        // The original check for 'Barnett's department' would follow, but it's now irrelevant
        // as the user-supplied query was not executed. Therefore, we return a failed result.
        return failed(this).feedback("sql-injection.arbitrary-query-blocked").build();

      } catch (SQLException sqle) {
        // Catch SQL exceptions from the dummy query or connection issues
        return failed(this).output(sqle.getMessage()).build();
      }
    } catch (Exception e) {
      return failed(this).output(this.getClass().getName() + " : " + e.getMessage()).build();
    }
  }
}
