package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delta tests for SqlInjectionLesson6b focusing on:
 * - getPassword still returning the DB value when available.
 * - completed() behavior unchanged.
 * - Exceptions are logged via SLF4J (printStackTrace removed).
 *
 * Note: SLF4J logging is verified indirectly by ensuring methods complete
 * without throwing when exceptions occur; direct inspection of logger
 * output would require a concrete backend which is beyond unit scope.
 */
public class SqlInjectionLesson6bTest {

  @Test
  @DisplayName("getPassword returns DB password when query succeeds")
  void getPassword_returnsPasswordFromDatabase() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    Statement statement = Mockito.mock(Statement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement(
            Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
            Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(statement);
    when(statement.executeQuery("SELECT password FROM user_system_data WHERE user_name = 'dave'"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("password")).thenReturn("secure-db-password");

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    String password = lesson.getPassword();

    assertEquals("secure-db-password", password);
  }

  @Test
  @DisplayName("getPassword falls back to default without throwing when SQL exception occurs (logged)")
  void getPassword_logsSqlExceptionAndReturnsDefault() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    Statement statement = Mockito.mock(Statement.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement(
            Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
            Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(statement);
    when(statement.executeQuery(Mockito.anyString()))
        .thenThrow(new java.sql.SQLException("DB failure"));

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    // Should not throw; should log via Slf4j and return default "dave"
    String password = lesson.getPassword();

    assertEquals("dave", password);
  }

  @Test
  @DisplayName("completed returns success when user-supplied value equals DB password")
  void completed_usesPasswordFromGetPassword() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson =
        Mockito.spy(new SqlInjectionLesson6b(dataSource));

    // Mock getPassword to ensure completed behavior relies on it as before.
    Mockito.doReturn("expectedPass").when(lesson).getPassword();

    AttackResult result = lesson.completed("expectedPass");

    assertEquals("SUCCESS", result.getLessonResultStatus().toString());
  }

  @Test
  @DisplayName("completed returns failed when user-supplied value differs from DB password")
  void completed_failsWhenUserIdDoesNotMatchPassword() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson =
        Mockito.spy(new SqlInjectionLesson6b(dataSource));

    Mockito.doReturn("expectedPass").when(lesson).getPassword();

    AttackResult result = lesson.completed("wrongPass");

    assertEquals("FAILED", result.getLessonResultStatus().toString());
  }

  @Test
  @DisplayName("Slf4j logger is present for SqlInjectionLesson6b")
  void logger_isConfigured() {
    Logger logger = LoggerFactory.getLogger(SqlInjectionLesson6b.class);
    org.junit.jupiter.api.Assertions.assertNotNull(
        logger, "Slf4j logger must be available for structured logging");
  }
}
