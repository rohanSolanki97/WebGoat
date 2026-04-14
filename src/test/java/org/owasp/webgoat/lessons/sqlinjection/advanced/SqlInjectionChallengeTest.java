package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on the SQL injection fix:
 * - Ensures a parameterized query with placeholder is used.
 * - Ensures user input is bound via setString.
 */
public class SqlInjectionChallengeTest {

  @Test
  void usesParameterizedQueryAndBindsInputName() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement statement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);

    String name = "John";

    // Act
    AttackResult result = challenge.completed(name);

    // Assert
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String sql = sqlCaptor.getValue();
    assertEquals(
        "SELECT * FROM employees WHERE first_name = ?",
        sql,
        "SQL should use parameterized query with placeholder");

    verify(statement).setString(1, name);

    org.junit.jupiter.api.Assertions.assertNotNull(result);
  }

  @Test
  void rejectsEmptyNameInput() {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    AttackResult result = challenge.completed("  ");

    assertFalse(result.getLessonCompleted(), "Empty name input should not complete the lesson");
  }
}
