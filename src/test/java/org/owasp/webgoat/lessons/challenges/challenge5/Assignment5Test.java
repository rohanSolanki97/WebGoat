package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.lessons.challenges.Flags;

/**
 * Delta tests for Assignment5 focusing on the change from string-concatenated SQL
 * to a parameterized PreparedStatement for the login query.
 */
class Assignment5Test {

  @Test
  void login_usesParameterizedQueryAndBindsUserInputs() throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Flags flags = mock(Flags.class);
    Assignment5 assignment = new Assignment5(dataSource, flags);

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(flags.getFlag(5)).thenReturn("FLAG5");

    String username = "Larry";
    String password = "Secr3t!";

    AttackResult result = assignment.login(username, password);

    // Assert SQL is parameterized, not concatenated with user input
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String sql = sqlCaptor.getValue();
    assertEquals(
        "select password from challenge_users where userid = ? and password = ?",
        sql);

    // Assert bound parameters match user-controlled input exactly
    verify(preparedStatement).setString(1, username);
    verify(preparedStatement).setString(2, password);

    assertTrue(result.getLessonCompleted());
  }

  @Test
  void login_doesNotQueryDatabaseForNonLarryUser() throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Flags flags = mock(Flags.class);
    Assignment5 assignment = new Assignment5(dataSource, flags);

    AttackResult result = assignment.login("Bob", "pwd");

    assertFalse(result.getLessonCompleted());
    // Critical: ensure no SQL is executed when the precondition (Larry) fails
    verifyNoInteractions(dataSource);
  }
}
