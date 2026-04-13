package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on the parameterized SELECT query used
 * to check for existing users and its resilience against SQL injection.
 */
class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_existingUser_usesParameterizedSelectAndFails() throws SQLException {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement selectPs = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(selectPs);
    when(selectPs.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);

    AttackResult result =
        challenge.registerNewUser("alice", "alice@example.com", "password123");

    verify(connection)
        .prepareStatement("select userid from sql_challenge_users where userid = ?");
    verify(selectPs).setString(1, "alice");
    assertEquals(AttackResult.Status.FAILURE, result.getStatus());
  }

  @Test
  void registerNewUser_injectionPayloadIsBoundAsParameterAndDoesNotCauseBypass()
      throws SQLException {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement selectPs = mock(PreparedStatement.class);
    PreparedStatement insertPs = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(selectPs);
    when(selectPs.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false);
    when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertPs);

    String injection = "bob' OR '1'='1";

    AttackResult result =
        challenge.registerNewUser(injection, "bob@example.com", "password123");

    // The injection string must be treated strictly as a parameter value
    verify(selectPs).setString(1, injection);
    verify(insertPs).setString(1, injection);
    verify(insertPs).setString(2, "bob@example.com");
    verify(insertPs).setString(3, "password123");

    // With no existing user, the flow should proceed to insertion and return an
    // informational (non-failure) result, indicating that injection did not
    // alter control flow of the SELECT statement.
    assertEquals(AttackResult.Status.INFO, result.getStatus());
  }
}
