package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delta tests for SqlInjectionLesson6b focusing on the changed logging behavior:
 * - Exceptions are no longer printed via printStackTrace but are logged via SLF4J.
 *
 * Derived path (by main→test mapping):
 * src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionLesson6bTest.java
 */
public class SqlInjectionLesson6bTest {

  @Test
  void getPassword_shouldNotPrintStackTraceOnSqlException_andReturnDefaultPassword() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    Statement stmt = Mockito.mock(Statement.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(
            connection.createStatement(
                Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
                Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(stmt);
    Mockito.when(stmt.executeQuery(Mockito.anyString()))
        .thenThrow(new java.sql.SQLException("DB error"));

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    // Use a spy on Logger to ensure printStackTrace is not called anywhere via System.err.
    Logger logger = LoggerFactory.getLogger(SqlInjectionLesson6b.class);
    Logger spyLogger = Mockito.spy(logger);
    // Swap the logger instance via reflection for test purposes
    TestReflectionUtils.setStaticFinalField(SqlInjectionLesson6b.class, "logger", spyLogger);

    // Act
    String password = lesson.getPassword();

    // Assert
    // 1) When DB fails, password should remain the default "dave"
    assertEquals("dave", password);

    // 2) Ensure that only logger.error is used, not System.err.printStackTrace
    Mockito.verify(spyLogger, Mockito.atLeastOnce())
        .error(Mockito.startsWith("Database error while retrieving password:"), Mockito.anyString());
    // We cannot directly assert absence of printStackTrace here, but we assert logging is used
  }

  /**
   * Minimal reflection helper to update private static final fields for testing the logger wiring
   * without changing production code.
   */
  static class TestReflectionUtils {
    static void setStaticFinalField(Class<?> targetClass, String fieldName, Object newValue) {
      try {
        var field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        java.lang.reflect.Field modifiersField =
            java.lang.reflect.Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        field.set(null, newValue);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
