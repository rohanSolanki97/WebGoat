package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.LessonDataSource;
import org.slf4j.Logger;

class SqlInjectionLesson6bTest {

  @Test
  void getPassword_shouldLogSqlExceptionInsteadOfPrintingStackTrace() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    Connection connection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenThrow(new SQLException("DB error"));

    SqlInjectionLesson6b spyLesson = spy(lesson);
    Logger loggerSpy = LoggerTestHelper.attachLoggerSpy(spyLesson);

    // Act
    String password = spyLesson.getPassword();

    // Assert: default password remains and error is logged
    org.junit.jupiter.api.Assertions.assertEquals("dave", password);
    verify(loggerSpy)
        .error(
            startsWith("SQL Exception encountered while fetching password for 'dave'"),
            any(SQLException.class));
  }

  @Test
  void getPassword_shouldLogGeneralExceptionInsteadOfPrintingStackTrace() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection failure"));

    SqlInjectionLesson6b spyLesson = spy(lesson);
    Logger loggerSpy = LoggerTestHelper.attachLoggerSpy(spyLesson);

    // Act
    String password = spyLesson.getPassword();

    // Assert
    org.junit.jupiter.api.Assertions.assertEquals("dave", password);
    verify(loggerSpy)
        .error(
            startsWith("General Exception encountered in getPassword method"),
            any(RuntimeException.class));
  }

  /**
   * Helper to attach a Mockito spy/mock to the Slf4j-generated 'log' field on the target.
   * Focuses the delta test on verifying log.error(...) is called instead of printStackTrace().
   */
  static class LoggerTestHelper {

    static Logger attachLoggerSpy(Object target) {
      try {
        java.lang.reflect.Field logField = target.getClass().getDeclaredField("log");
        logField.setAccessible(true);
        Logger loggerSpy = mock(Logger.class);
        logField.set(target, loggerSpy);
        return loggerSpy;
      } catch (Exception e) {
        throw new IllegalStateException("Unable to attach logger spy", e);
      }
    }
  }
}
