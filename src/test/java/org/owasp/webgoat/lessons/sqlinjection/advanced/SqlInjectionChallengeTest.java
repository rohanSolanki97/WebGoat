package org.owasp.webgoat.lessons.sqlinjection.advanced;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on the parameterized SELECT used for checking if
 * a user already exists.
 */
class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_shouldUsePreparedStatementForUserExistenceCheck() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkUserStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertStmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(
            "select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkUserStmt);
    Mockito.when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertStmt);
    Mockito.when(checkUserStmt.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false);

    String username = "newUser";
    String email = "user@example.com";
    String password = "pwd";

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert: existence check query is parameterized and binds the username
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    org.junit.jupiter.api.Assertions.assertEquals(
        "select userid from sql_challenge_users where userid = ?", sqlCaptor.getValue());
    Mockito.verify(checkUserStmt).setString(1, username);
    Mockito.verify(checkUserStmt).executeQuery();

    // Insert statement still uses parameters
    Mockito.verify(insertStmt).setString(1, username);
    Mockito.verify(insertStmt).setString(2, email);
    Mockito.verify(insertStmt).setString(3, password);
    Mockito.verify(insertStmt).execute();

    org.junit.jupiter.api.Assertions.assertFalse(result.getLessonCompleted());
  }

  @Test
  void registerNewUser_shouldNotTouchDatabaseWhenInputInvalid() {
    // Arrange: invalid (too long) username triggers validation failure and protects SQL path
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    String longUsername = "x".repeat(251);

    // Act
    AttackResult result =
        challenge.registerNewUser(longUsername, "e@example.com", "pwd");

    // Assert
    org.junit.jupiter.api.Assertions.assertFalse(result.getLessonCompleted());
    Mockito.verifyNoInteractions(dataSource);
  }
}
