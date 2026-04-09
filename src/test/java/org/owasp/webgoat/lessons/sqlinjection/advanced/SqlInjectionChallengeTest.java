package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on the change to a PreparedStatement
 * for the username existence check.
 */
public class SqlInjectionChallengeTest {

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
    Mockito
        .when(connection.prepareStatement(Mockito.startsWith("select userid from sql_challenge_users")))
        .thenReturn(checkUserStmt);
    Mockito
        .when(connection.prepareStatement(Mockito.startsWith("INSERT INTO sql_challenge_users")))
        .thenReturn(insertStmt);
    Mockito.when(checkUserStmt.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false);

    String username = "bob' OR '1'='1";
    String email = "bob@example.com";
    String password = "pass";

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert: verify parameterized query usage and binding
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();

    assertTrue(
        usedSql.contains("where userid = ?"),
        "User lookup must be parameterized and not concatenate the username directly");

    Mockito.verify(checkUserStmt).setString(1, username);
    Mockito.verify(insertStmt).setString(1, username);
    Mockito.verify(insertStmt).setString(2, email);
    Mockito.verify(insertStmt).setString(3, password);

    // We do not assert lesson completion here; focus is on secure SQL construction.
    assertTrue(result != null);
  }
}
