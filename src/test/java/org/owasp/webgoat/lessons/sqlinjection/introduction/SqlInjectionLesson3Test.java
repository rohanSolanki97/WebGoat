package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson3 focusing on:
 * - Removal of arbitrary SQL execution from user input.
 * - Use of parameterized update query with newDepartment argument.
 *
 * Derived path:
 * src/test/java/org/owasp/webgoat/lessons/sqlinjection/introduction/SqlInjectionLesson3Test.java
 */
public class SqlInjectionLesson3Test {

  private LessonDataSource dataSource;
  private SqlInjectionLesson3 lesson;

  @BeforeEach
  void setup() {
    dataSource = Mockito.mock(LessonDataSource.class);
    lesson = new SqlInjectionLesson3(dataSource);
  }

  @Test
  void completed_shouldUseParameterizedUpdateAndNotRawQuery() throws Exception {
    // Arrange
    String newDepartment = "Sales";
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement updateStmt = Mockito.mock(PreparedStatement.class);
    Statement checkStmt = Mockito.mock(Statement.class);
    ResultSet rs = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(
            connection.prepareStatement(
                Mockito.eq("UPDATE employees SET department = ? WHERE last_name = 'Barnett'")))
        .thenReturn(updateStmt);
    Mockito.when(
            connection.createStatement(
                Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
                Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(checkStmt);
    Mockito.when(
            checkStmt.executeQuery(
                Mockito.eq("SELECT * FROM employees WHERE last_name='Barnett';")))
        .thenReturn(rs);

    Mockito.when(rs.first()).thenReturn(true);
    Mockito.when(rs.getString("department")).thenReturn("Sales");

    // Act
    AttackResult result = lesson.completed(newDepartment);

    // Assert: 1) ensure PreparedStatement was used with expected SQL and parameter
    Mockito.verify(connection)
        .prepareStatement(
            Mockito.eq("UPDATE employees SET department = ? WHERE last_name = 'Barnett'"));
    Mockito.verify(updateStmt).setString(1, newDepartment);
    Mockito.verify(updateStmt).executeUpdate();

    // Assert: 2) injection-like department should not be executed as raw SQL
    ArgumentCaptor<String> anyRawSql = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection, Mockito.never()).createStatement(); // no generic, unparameterized Statement for updates

    // Assert: 3) lesson can still succeed with the secure flow
    assertTrue(result.isSuccess());
  }

  @Test
  void completed_shouldNotExecuteArbitrarySqlFromUserInput() throws Exception {
    // Arrange
    String malicious = "Sales'; DROP TABLE employees; --";
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement updateStmt = Mockito.mock(PreparedStatement.class);
    Statement checkStmt = Mockito.mock(Statement.class);
    ResultSet rs = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(
            connection.prepareStatement(
                Mockito.eq("UPDATE employees SET department = ? WHERE last_name = 'Barnett'")))
        .thenReturn(updateStmt);
    Mockito.when(
            connection.createStatement(
                Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
                Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(checkStmt);
    Mockito.when(
            checkStmt.executeQuery(
                Mockito.eq("SELECT * FROM employees WHERE last_name='Barnett';")))
        .thenReturn(rs);

    Mockito.when(rs.first()).thenReturn(true);
    Mockito.when(rs.getString("department")).thenReturn("Sales");

    // Act
    AttackResult result = lesson.completed(malicious);

    // Assert: 1) malicious string is only ever bound as a parameter
    Mockito.verify(updateStmt).setString(1, malicious);

    // Assert: 2) arbitrary SQL text is never executed directly
    Mockito.verify(connection, Mockito.never()).createStatement(Mockito.anyInt(), Mockito.anyInt());
    assertFalse(result.isFailure());
  }
}
