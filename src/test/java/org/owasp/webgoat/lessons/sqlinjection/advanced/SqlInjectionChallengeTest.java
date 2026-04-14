package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/*
 * Delta test for:
 *   Source: src/main/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallenge.java
 *   Test:   src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallengeTest.java
 *
 * Focus: change from concatenated SQL with Statement to parameterized PreparedStatement
 * for checkUserQuery.
 */
public class SqlInjectionChallengeTest {

  private LessonDataSource dataSource;
  private SqlInjectionChallenge challenge;
  private Connection connection;
  private PreparedStatement checkUserStatement;
  private PreparedStatement insertStatement;
  private ResultSet resultSet;

  @BeforeEach
  void setup() throws Exception {
    dataSource = Mockito.mock(LessonDataSource.class);
    challenge = new SqlInjectionChallenge(dataSource);

    connection = Mockito.mock(Connection.class);
    checkUserStatement = Mockito.mock(PreparedStatement.class);
    insertStatement = Mockito.mock(PreparedStatement.class);
    resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);

    Mockito.when(
            connection.prepareStatement(
                "select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkUserStatement);
    Mockito.when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertStatement);

    Mockito.when(checkUserStatement.executeQuery()).thenReturn(resultSet);
  }

  @Test
  void registerNewUserShouldUseParameterizedCheckUserQuery() {
    // Arrange
    String username = "newuser";
    String email = "user@example.com";
    String password = "pwd";
    Mockito.when(resultSet.next()).thenReturn(false);

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert: parameterized SQL and bound parameter
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();

    assertEquals(
        "select userid from sql_challenge_users where userid = ?",
        usedSql,
        "User existence check must use parameter marker");

    verify(checkUserStatement).setString(1, username);
  }

  @Test
  void registerNewUserShouldTreatMaliciousUsernameAsData() {
    // Arrange
    String maliciousUsername = "victim' OR '1'='1";
    String email = "user@example.com";
    String password = "pwd";
    Mockito.when(resultSet.next()).thenReturn(false);

    // Act
    AttackResult result = challenge.registerNewUser(maliciousUsername, email, password);

    // Assert: same parameterized query; malicious input bound as value
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();

    assertEquals(
        "select userid from sql_challenge_users where userid = ?",
        usedSql,
        "checkUserQuery must remain parameterized under malicious input");

    verify(checkUserStatement).setString(1, maliciousUsername);
  }
}
