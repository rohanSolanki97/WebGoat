package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on the vulnerability:
 * "Change this code to not construct SQL queries directly from user-controlled data."
 *
 * The fix replaces a concatenated SELECT query with a parameterized PreparedStatement.
 * These tests ensure:
 * - The SELECT uses a PreparedStatement with a parameter placeholder.
 * - User-controlled username is bound via setString on the PreparedStatement.
 */
class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_usesPreparedStatementForUserExistenceCheck() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement selectStatement = mock(PreparedStatement.class);
    PreparedStatement insertStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(eq("select userid from sql_challenge_users where userid = ?")))
        .thenReturn(selectStatement);
    when(selectStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);
    when(connection.prepareStatement(eq("INSERT INTO sql_challenge_users VALUES (?, ?, ?)")))
        .thenReturn(insertStatement);

    String username = "newuser";
    String email = "newuser@example.com";
    String password = "StrongP@ssw0rd";

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert
    // Delta: assert parametrization and binding on the SELECT.
    verify(connection)
        .prepareStatement(eq("select userid from sql_challenge_users where userid = ?"));
    verify(selectStatement).setString(1, username);
    verify(selectStatement).executeQuery();

    // Ensure API is still returning a non-null AttackResult.
    assertNotNull(result, "registerNewUser should return a non-null AttackResult instance");
  }

  @Test
  void registerNewUser_usesParameterizedQueryEvenWithInjectionPayload() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement selectStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(eq("select userid from sql_challenge_users where userid = ?")))
        .thenReturn(selectStatement);
    when(selectStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    // Injection-like username that previously would have affected the SQL WHERE clause.
    String username = "user' OR '1'='1";
    String email = "a@b.c";
    String password = "pwd";

    // Act
    challenge.registerNewUser(username, email, password);

    // Assert
    // The username must be bound as data, not concatenated into SQL.
    verify(connection)
        .prepareStatement(eq("select userid from sql_challenge_users where userid = ?"));
    verify(selectStatement).setString(1, username);
  }
}
