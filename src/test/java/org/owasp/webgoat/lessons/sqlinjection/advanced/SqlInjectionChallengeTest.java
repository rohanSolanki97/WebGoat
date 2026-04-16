package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on the change from concatenated SQL using
 * Statement to a parameterized PreparedStatement for user lookups.
 *
 * These tests validate that:
 * - The username is bound through a PreparedStatement parameter.
 * - The behavior (existing vs new user) remains correct.
 */
public class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_usesPreparedStatementForUserLookup_nonExistingUserFlow()
      throws SQLException {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertStmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkStmt);
    when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertStmt);
    when(checkStmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    String username = "newuser";
    String email = "user@example.com";
    String password = "pass123";

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert: verify safe parameter binding for user lookup
    verify(checkStmt).setString(1, username);
    verify(checkStmt).executeQuery();

    // Verify user creation still occurs for non-existing user
    verify(insertStmt).setString(1, username);
    verify(insertStmt).setString(2, email);
    verify(insertStmt).setString(3, password);
    // Lesson semantics are informational; should not be marked completed here
    assertFalse(result.getLessonCompleted());
  }

  @Test
  void registerNewUser_existingUserDetectedWithPreparedStatement() throws SQLException {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkStmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);

    String username = "existing";
    String email = "existing@example.com";
    String password = "pwd";

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert
    verify(checkStmt).setString(1, username);
    assertFalse(
        result.getLessonCompleted(),
        "Existing user should not produce a completed lesson state");
  }
}
