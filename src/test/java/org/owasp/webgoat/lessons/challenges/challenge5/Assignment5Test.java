package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
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
import org.owasp.webgoat.lessons.challenges.Flags;

/**
 * Delta tests for Assignment5 focusing on the SQL injection fix:
 * - Ensures PreparedStatement with parameter placeholders is used.
 * - Ensures user input is bound via setString rather than concatenated into SQL.
 */
public class Assignment5Test {

  @Test
  void loginUsesParameterizedQueryAndBindsUserInput() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Flags flags = Mockito.mock(Flags.class);
    Assignment5 assignment5 = new Assignment5(dataSource, flags);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(flags.getFlag(5)).thenReturn("FLAG-5");

    String username = "Larry";
    String password = "securePass123";

    // Act
    AttackResult result = assignment5.login(username, password);

    // Assert: verify prepared statement SQL and parameters
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String sql = sqlCaptor.getValue();
    assertEquals(
        "select password from challenge_users where userid = ? and password = ?",
        sql,
        "SQL should use parameter placeholders, not string concatenation");

    verify(preparedStatement).setString(1, eq(username));
    verify(preparedStatement).setString(2, eq(password));

    // Successful query should produce a non-null result
    org.junit.jupiter.api.Assertions.assertNotNull(result);
  }

  @Test
  void loginFailsForNonLarryUserEvenWithValidPassword() throws Exception {
    // This test asserts behavior is preserved while using parameterized queries.
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Flags flags = Mockito.mock(Flags.class);
    Assignment5 assignment5 = new Assignment5(dataSource, flags);

    String username = "Eve";
    String password = "anything";

    AttackResult result = assignment5.login(username, password);

    assertFalse(result.getLessonCompleted(), "Non-Larry user should not complete the lesson");
  }
}
