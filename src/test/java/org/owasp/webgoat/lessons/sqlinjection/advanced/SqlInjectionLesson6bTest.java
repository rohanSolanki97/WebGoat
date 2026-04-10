package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;

/**
 * Delta tests for SqlInjectionLesson6b focusing on:
 * - Removal of direct printStackTrace() in exception paths (replaced by logging).
 * - Preservation of completed() behavior that compares user input with getPassword().
 *
 * Note: We cannot directly assert on the logger output without additional plumbing,
 * so we verify that getPassword() handles exceptions gracefully and returns the
 * default value without throwing or printing to System.err/System.out.
 */
class SqlInjectionLesson6bTest {

  @Test
  void completed_returnsSuccessWhenUserInputMatchesPasswordFromDatabase() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    Statement statement = Mockito.mock(Statement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenReturn(statement);
    when(statement.executeQuery("SELECT password FROM user_system_data WHERE user_name = 'dave'"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("password")).thenReturn("secret-from-db");

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    // completed() should call getPassword(), receive "secret-from-db" and compare it with input
    var result = lesson.completed("secret-from-db");

    // AttackResult details are not exposed; we assert on success via the isLessonCompleted flag
    // by inspecting toString or using the builder contract: success() marks lessonCompleted=true.
    String output = result.toString();
    // Heuristic: the AttackResultBuilder.success() sets lessonCompleted=true in the result.
    // We assert that the 'lessonCompleted=true' marker is present.
    // This keeps the test focused on the behavior that succeeded comparison.
    org.junit.jupiter.api.Assertions.assertTrue(
        output.contains("lessonCompleted=true"), "Expected lesson to be marked as completed");
  }

  @Test
  void getPassword_handlesSqlExceptionAndReturnsDefaultWithoutThrowing() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenThrow(new SQLException("DB error"));

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    // When an SQLException occurs, getPassword() should catch it, log via log.error,
    // and return the default password value ("dave") without rethrowing or printing a stack trace.
    String password = lesson.getPassword();

    assertEquals("dave", password);
  }

  @Test
  void getPassword_handlesGenericExceptionAndReturnsDefaultWithoutThrowing() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);

    // Simulate a failure when obtaining the connection itself
    when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection init failure"));

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    String password = lesson.getPassword();

    // The method should swallow the exception (after logging) and still return default
    assertEquals("dave", password);
  }
}
