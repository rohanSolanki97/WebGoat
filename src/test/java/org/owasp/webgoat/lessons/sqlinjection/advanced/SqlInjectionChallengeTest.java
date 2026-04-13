package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on the hardened user-existence
 * check that now uses a parameterized PreparedStatement instead of string concatenation.
 */
class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_usesPreparedStatementForUserLookup() throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement userCheckStmt = mock(PreparedStatement.class);
    PreparedStatement insertStmt = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(startsWith("select userid"))).thenReturn(userCheckStmt);
    when(connection.prepareStatement(startsWith("INSERT INTO sql_challenge_users")))
        .thenReturn(insertStmt);
    when(userCheckStmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false); // user does not exist

    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    String username = "user1";
    String email = "user1@example.com";
    String password = "p@ssw0rd";

    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert that a parameterized query is used
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String sql = sqlCaptor.getValue();
    assertEquals(
        "select userid from sql_challenge_users where userid = ?",
        sql);

    // Assert the username is bound as a parameter, rather than concatenated
    verify(userCheckStmt).setString(1, username);

    assertNotNull(result);
  }

  @Test
  void registerNewUser_doesNotHitDatabaseWhenInputInvalid() {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    AttackResult result =
        challenge.registerNewUser("", "short@example.com", "pwd");

    assertFalse(result.getLessonCompleted());
    verifyNoInteractions(dataSource);
  }
}
