package org.owasp.webgoat.lessons.challenges.challenge5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.lessons.challenges.Flags;

/**
 * Delta tests for Assignment5 focusing on using PreparedStatement instead of string concatenation
 * for SQL, ensuring user input is bound as parameters.
 */
class Assignment5Test {

  @Test
  void login_shouldUseParameterizedQueryForUsernameAndPassword() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Flags flags = Mockito.mock(Flags.class);
    Assignment5 assignment5 = new Assignment5(dataSource, flags);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(true);
    Mockito.when(flags.getFlag(5)).thenReturn("FLAG-5");

    String username = "Larry";
    String password = "somePassword";

    // Act
    AttackResult result = assignment5.login(username, password);

    // Assert: SQL uses placeholders and parameters are bound correctly
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String sqlUsed = sqlCaptor.getValue();
    org.junit.jupiter.api.Assertions.assertEquals(
        "select password from challenge_users where userid = ? and password = ?",
        sqlUsed);

    Mockito.verify(preparedStatement).setString(1, username);
    Mockito.verify(preparedStatement).setString(2, password);
    Mockito.verify(preparedStatement).executeQuery();
    org.junit.jupiter.api.Assertions.assertTrue(result.getLessonCompleted());
  }

  @Test
  void login_shouldShortCircuitForNonLarryUserWithoutQuery() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Flags flags = Mockito.mock(Flags.class);
    Assignment5 assignment5 = new Assignment5(dataSource, flags);

    // Act
    AttackResult result = assignment5.login("NotLarry", "pwd");

    // Assert: unchanged guard that also protects the SQL path
    org.junit.jupiter.api.Assertions.assertFalse(result.getLessonCompleted());
    Mockito.verifyNoInteractions(dataSource);
  }
}
