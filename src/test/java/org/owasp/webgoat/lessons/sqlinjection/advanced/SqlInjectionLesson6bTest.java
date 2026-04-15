package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

public class SqlInjectionLesson6bTest {

  @Test
  void completed_returnsFailedWhenFallbackPasswordUsedOnException() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    // Simulate an exception when obtaining connection so getPassword falls back to default "dave"
    when(dataSource.getConnection()).thenThrow(new RuntimeException("DB not available"));

    // Act
    AttackResult result = lesson.completed("not_dave");

    // Assert
    // Even when an exception occurs, completed() still compares input with getPassword()
    // (which returns the fallback "dave") and returns a failed AttackResult.
    assertFalse(result.getLessonCompleted());
  }

  @Test
  void completed_returnsSuccessWhenUserMatchesPasswordFromDatabase() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

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
    when(resultSet.getString("password")).thenReturn("secret_from_db");

    // Act
    AttackResult result = lesson.completed("secret_from_db");

    // Assert
    // Verifies that getPassword() still returns the DB value and completed() compares against it.
    assertTrue(result.getLessonCompleted());
  }
}
