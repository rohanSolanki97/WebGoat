package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;

/*
 * Resolved test path (derived from src/main/java → src/test/java):
 * src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionLesson6bTest.java
 */
public class SqlInjectionLesson6bTest {

  @Test
  @DisplayName("getPassword returns default value on SQL exception without propagating stack trace behavior")
  void getPassword_handlesSqlExceptionWithoutChangingBehavior() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    Statement statement = Mockito.mock(Statement.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito
        .when(connection.createStatement(
            Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
            Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(statement);
    // Force a SQLException to trigger the error-handling/logging path
    Mockito.when(statement.executeQuery(Mockito.anyString()))
        .thenThrow(new SQLException("simulated error"));

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    // Act
    String password = lesson.getPassword();

    // Assert
    // Behavior-oriented assertion: method should still return the default value
    // and not propagate the SQLException.
    assertEquals("dave", password, "Default password value should be returned on SQL error");

    // Also verify that the database interaction took place exactly once, ensuring
    // we exercised the error-handling path where logging was introduced.
    Mockito.verify(connection, times(1))
        .createStatement(
            Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
            Mockito.eq(ResultSet.CONCUR_READ_ONLY));
  }
}
