package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on changing the user lookup query to a
 * parameterized PreparedStatement instead of concatenated SQL.
 *
 * Path: src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallengeTest.java
 */
public class SqlInjectionChallengeTest {

  private LessonDataSource dataSource;
  private SqlInjectionChallenge challenge;

  @BeforeEach
  void setup() {
    dataSource = Mockito.mock(LessonDataSource.class);
    challenge = new SqlInjectionChallenge(dataSource);
  }

  @Test
  void registerNewUser_shouldUsePreparedStatementForUserExistenceCheck() throws SQLException {
    // Arrange
    String username = "alice' OR '1'='1";
    String email = "a@example.com";
    String password = "pw";
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement selectStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertStmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.startsWith("select userid"))).thenReturn(selectStmt);
    Mockito.when(connection.prepareStatement(Mockito.startsWith("INSERT INTO sql_challenge_users")))
        .thenReturn(insertStmt);
    Mockito.when(selectStmt.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false); // user does not exist

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert: 1) SELECT should use placeholder, not direct concatenation
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();
    assertTrue(
        usedSql.contains("where userid = ?"),
        "User lookup SQL should be parameterized");

    // Assert: 2) User input is bound via setString, not concatenated
    Mockito.verify(selectStmt).setString(1, username);

    // Assert: 3) Injection-like username should not cause automatic failure or bypass
    assertFalse(result.isFailure());
  }

  @Test
  void registerNewUser_shouldStillInsertUserWithSafeQuery() throws SQLException {
    // Arrange
    String username = "bob";
    String email = "b@example.com";
    String password = "secret";
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement selectStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertStmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.startsWith("select userid"))).thenReturn(selectStmt);
    Mockito.when(connection.prepareStatement(Mockito.startsWith("INSERT INTO sql_challenge_users")))
        .thenReturn(insertStmt);
    Mockito.when(selectStmt.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false);

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert: insert using prepared statement is still called correctly
    Mockito.verify(insertStmt).setString(1, username);
    Mockito.verify(insertStmt).setString(2, email);
    Mockito.verify(insertStmt).setString(3, password);
    Mockito.verify(insertStmt).execute();
    assertFalse(result.isFailure());
  }
}
