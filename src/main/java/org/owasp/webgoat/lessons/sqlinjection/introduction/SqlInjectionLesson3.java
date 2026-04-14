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
import java.sql.PreparedStatement; // Added import
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
  public AttackResult completed(@RequestParam String newDepartment) { // Changed parameter name
    return injectableQuery(newDepartment);
  }

  protected AttackResult injectableQuery(String newDepartment) { // Changed parameter name
    try (Connection connection = dataSource.getConnection()) {
      // Remediation: Use PreparedStatement for the UPDATE query to prevent SQL Injection.
      // The user-supplied 'newDepartment' is now treated as a parameter, not executable SQL.
      try (PreparedStatement updateStatement =
          connection.prepareStatement("UPDATE employees SET department = ? WHERE last_name = 'Barnett'")) {
        updateStatement.setString(1, newDepartment);
        updateStatement.executeUpdate();

        // The checkStatement part remains the same, as it's a fixed query for verification
        try (Statement checkStatement =
            connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)) {
          ResultSet results =
              checkStatement.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';");
          StringBuilder output = new StringBuilder();
          // user completes lesson if the department of Tobi Barnett now is 'Sales'
          results.first();
          if (results.getString("department").equals("Sales")) {
            output.append("<span class='feedback-positive'>" + newDepartment + "</span>"); // Use newDepartment
            output.append(SqlInjectionLesson8.generateTable(results));
            return success(this).output(output.toString()).build();
          } else {
            return failed(this).output(output.toString()).build();
          }
        }
      } catch (SQLException sqle) {
        return failed(this).output(sqle.getMessage()).build();
      }
    } catch (Exception e) {
      return failed(this).output(this.getClass().getName() + " : " + e.getMessage()).build();
    }
  }
}
