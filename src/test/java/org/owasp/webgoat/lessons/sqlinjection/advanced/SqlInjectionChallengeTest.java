package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
 * File path (derived from main source): src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallengeTest.java
 *
 * Delta tests for SqlInjectionChallenge focusing on:
 * - the user existence check using a parameterized PreparedStatement,
 * - user input being bound as a parameter, preventing SQL injection via username.
 */
class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_usesParameterizedCheckUserQueryAndStillInsertsForNewUser() throws SQLException {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkUserStmt = Mockito.mock(PreparedStatement.class);
    ResultSet checkUserResult = Mockito.mock(ResultSet.class);
    PreparedStatement insertStmt = Mockito.mock(PreparedStatement.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.startsWith("select userid")))
        .thenReturn(checkUserStmt);
    Mockito.when(connection.prepareStatement(Mockito.startsWith("INSERT INTO sql_challenge_users")))
        .thenReturn(insertStmt);
    Mockito.when(checkUserStmt.executeQuery()).thenReturn(checkUserResult);
    Mockito.when(checkUserResult.next()).thenReturn(false); // user does not yet exist

    AttackResult result =
        challenge.registerNewUser("newUser", "user@example.com", "pwd123");

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();
    assertEquals(
        "select userid from sql_challenge_users where userid = ?",
        usedSql,
        "checkUserQuery must be parameterized");

    Mockito.verify(checkUserStmt).setString(1, "newUser");

    Mockito.verify(insertStmt).setString(1, "newUser");
    Mockito.verify(insertStmt).setString(2, "user@example.com");
    Mockito.verify(insertStmt).setString(3, "pwd123");
    Mockito.verify(insertStmt).execute();

    assertFalse(
        result.getLessonCompleted(),
        "User creation remains informational; lesson completion semantics are unchanged by the fix");
  }

  @Test
  void registerNewUser_treatsInjectionPayloadAsDataInUsername() throws SQLException {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkUserStmt = Mockito.mock(PreparedStatement.class);
    ResultSet checkUserResult = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(checkUserStmt);
    Mockito.when(checkUserStmt.executeQuery()).thenReturn(checkUserResult);
    Mockito.when(checkUserResult.next()).thenReturn(false);

    String injectionUsername = "user' OR '1'='1";

    challenge.registerNewUser(injectionUsername, "mail@ex.com", "pwd");

    Mockito.verify(checkUserStmt).setString(1, injectionUsername);
  }
}
