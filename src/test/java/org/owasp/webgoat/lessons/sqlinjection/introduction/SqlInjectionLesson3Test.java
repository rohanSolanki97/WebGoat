package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson3 focusing on the constrained, parameterized UPDATE
 * for Barnett's department and rejection of arbitrary SQL.
 */
class SqlInjectionLesson3Test {

  @Test
  void injectableQuery_allowsValidUpdatePatternAndMarksSuccessWhenDepartmentSales()
      throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement updatePs = mock(PreparedStatement.class);
    Statement checkStatement = mock(Statement.class);
    ResultSet rs = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(
            "UPDATE employees SET department = ? WHERE last_name = 'Barnett'"))
        .thenReturn(updatePs);
    when(connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';"))
        .thenReturn(rs);
    when(rs.first()).thenReturn(true);
    when(rs.getString("department")).thenReturn("Sales");

    String query =
        "UPDATE employees SET department = 'Sales' WHERE last_name = 'Barnett';";

    AttackResult result = lesson.injectableQuery(query);

    verify(connection)
        .prepareStatement(
            "UPDATE employees SET department = ? WHERE last_name = 'Barnett'");
    verify(updatePs).setString(1, "Sales");
    verify(updatePs).executeUpdate();

    verify(checkStatement)
        .executeQuery("SELECT * FROM employees WHERE last_name='Barnett';");
    assertEquals(AttackResult.Status.SUCCESS, result.getStatus());
  }

  @Test
  void injectableQuery_rejectsMalformedOrMaliciousQueryWithoutExecutingUpdate()
      throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection);

    String malicious =
        "UPDATE employees SET salary = 999999 WHERE last_name = 'Barnett';";

    AttackResult result = lesson.injectableQuery(malicious);

    verify(connection, never())
        .prepareStatement(anyString());
    verify(connection, never()).createStatement();

    assertEquals(AttackResult.Status.FAILURE, result.getStatus());
  }
}
