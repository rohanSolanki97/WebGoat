/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
public class SqlInjectionLesson6b implements AssignmentEndpoint {
  private static final Logger logger = LoggerFactory.getLogger(SqlInjectionLesson6b.class);
  private final LessonDataSource dataSource;

  public SqlInjectionLesson6b(LessonDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @PostMapping("/SqlInjectionAdvanced/attack6b")
  @ResponseBody
  public AttackResult completed(@RequestParam String userid_6b, HttpServletResponse response) throws IOException {
    // Set a secure cookie for demonstration, unrelated to the lesson's success logic
    Cookie secureCookie = new Cookie("session_id", "some_secure_value");
    secureCookie.setHttpOnly(true);
    secureCookie.setSecure(true); // Ensure 'Secure' attribute is set
    secureCookie.setPath("/");
    response.addCookie(secureCookie);

    if (userid_6b.equals(getPassword())) {
      return success(this).build();
    } else {
      return failed(this).build();
    }
  }

  protected String getPassword() {
    String password = "dave";
    try (Connection connection = dataSource.getConnection()) {
      String query = "SELECT password FROM user_system_data WHERE user_name = 'dave'";
      try {
        Statement statement =
            connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet results = statement.executeQuery(query);

        if (results != null && results.first()) {
          password = results.getString("password");
        }
      } catch (SQLException sqle) {
        // Log the exception for debugging, but avoid exposing sensitive details
        logger.error("SQL Exception in getPassword: {}", sqle.getMessage());
      }
    } catch (Exception e) {
      // Log the exception for debugging, but avoid exposing sensitive details
      logger.error("General Exception in getPassword: {}", e.getMessage());
    }
    return (password);
  }
}
