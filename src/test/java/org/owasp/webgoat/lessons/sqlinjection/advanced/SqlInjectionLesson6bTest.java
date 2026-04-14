package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Test file path (derived):
 * src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionLesson6bTest.java
 *
 * Delta tests for SqlInjectionLesson6b focusing on:
 * - preserving functional behavior of completed(),
 * - ensuring getPassword() no longer prints stack traces directly (logging used instead).
 *
 * Note: Direct verification of logging requires a logging test appender which is outside the
 * current dependencies; this test asserts that exceptions during getPassword() do not break the
 * flow and that lesson behavior remains unchanged.
 */
class SqlInjectionLesson6bTest {

  @Test
  void completed_returnsFailedWhenUseridDoesNotMatchPassword() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    Statement statement = Mockito.mock(Statement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito
        .when(connection.createStatement(
            Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
            Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(statement);
    Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
    Mockito.when(resultSet.first()).thenReturn(true);
    Mockito.when(resultSet.getString("password")).thenReturn("secret-password");

    AttackResult result = lesson.completed("wrong-value");

    assertFalse(
        result.getLessonCompleted(),
        "If supplied userid does not match the DB password, lesson should not be completed");
  }

  @Test
  void getPassword_handlesSqlExceptionGracefully() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito
        .when(connection.createStatement(
            Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
            Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenThrow(new RuntimeException("DB down"));

    String password = lesson.getPassword();

    // Even if logging happens internally, getPassword() must still return a non-null value.
    org.junit.jupiter.api.Assertions.assertNotNull(
        password, "getPassword() should handle exceptions and still return a value");
  }
}
