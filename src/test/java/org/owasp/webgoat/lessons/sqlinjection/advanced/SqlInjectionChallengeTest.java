package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;

/**
 * Delta tests for SqlInjectionChallenge focusing on the changed behavior:
 * - Use of PreparedStatement with placeholders instead of string concatenation for user lookup.
 *
 * These tests verify:
 * - The SELECT query uses a single '?' placeholder and binds the username via setString.
 */
public class SqlInjectionChallengeTest {

  private LessonDataSource dataSource;
  private SqlInjectionChallenge challenge;
  private Connection connection;
  private PreparedStatement checkUserStatement;
  private PreparedStatement insertStatement;
  private ResultSet resultSet;

  @BeforeEach
  void setUp() throws Exception {
    dataSource = mock(LessonDataSource.class);
    connection = mock(Connection.class);
    checkUserStatement = mock(PreparedStatement.class);
    insertStatement = mock(PreparedStatement.class);
    resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkUserStatement);
    when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertStatement);
    when(checkUserStatement.executeQuery()).thenReturn(resultSet);

    challenge = new SqlInjectionChallenge(dataSource);
  }

  @Test
  void registerNewUser_usesParameterizedSelectForExistingUserCheck() {
    // Arrange
    String username = "alice";
    String email = "alice@example.com";
    String password = "secret";
    // Simulate that user does not exist so insert would be executed; we only care about select here
    when(() -> {
          try {
            return resultSet.next();
          } catch (Exception e) {
            return false;
          }
        })
        .thenReturn(false);

    // Act
    challenge.registerNewUser(username, email, password);

    // Assert
    verify(connection)
        .prepareStatement(eq("select userid from sql_challenge_users where userid = ?"));
    verify(checkUserStatement).setString(1, username);
    verify(checkUserStatement).executeQuery();
  }

  @Test
  void registerNewUser_usesParameterizedInsertWhenUserDoesNotExist() throws Exception {
    // Arrange
    String username = "bob";
    String email = "bob@example.com";
    String password = "secret2";
    when(resultSet.next()).thenReturn(false);

    // Act
    challenge.registerNewUser(username, email, password);

    // Assert: insert prepared statement is parameterized and binds all values
    verify(connection)
        .prepareStatement(eq("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"));
    verify(insertStatement).setString(1, username);
    verify(insertStatement).setString(2, email);
    verify(insertStatement).setString(3, password);
    verify(insertStatement).execute();
  }
}
