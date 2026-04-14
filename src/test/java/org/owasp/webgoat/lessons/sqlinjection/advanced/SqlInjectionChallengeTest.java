package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on using a parameterized PreparedStatement instead
 * of string-concatenated SQL for the user existence check.
 */
public class SqlInjectionChallengeTest {

  private LessonDataSource dataSource;
  private SqlInjectionChallenge challenge;

  private Connection connection;
  private PreparedStatement preparedStatement;
  private ResultSet resultSet;

  @BeforeEach
  void setup() throws Exception {
    dataSource = Mockito.mock(LessonDataSource.class);
    challenge = new SqlInjectionChallenge(dataSource);

    connection = Mockito.mock(Connection.class);
    preparedStatement = Mockito.mock(PreparedStatement.class);
    resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito
        .when(connection.prepareStatement(Mockito.anyString()))
        .thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
  }

  @Test
  void registerNewUser_usesPreparedStatementForUserLookup() throws Exception {
    // Arrange
    Mockito.when(resultSet.next()).thenReturn(false);

    String username = "newuser";
    String email = "user@example.com";
    String password = "password";

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert: ensure a parameterized query is used
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();
    assertTrue(
        usedSql.contains("where userid = ?"),
        "Expected user lookup query to use a parameter placeholder instead of concatenation");

    Mockito.verify(preparedStatement).setString(1, username);
    assertFalse(result.isLessonCompleted(), "User creation should be informational, not completion");
  }

  @Test
  void registerNewUser_sqlInjectionPayloadDoesNotAlterLookupLogic() throws Exception {
    // Arrange
    Mockito.when(resultSet.next()).thenReturn(false);

    String username = "victim";
    String email = "user@example.com";
    String password = "' OR '1'='1";

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert: injection in password must not affect the user existence check query
    Mockito.verify(preparedStatement).setString(1, username);
    assertFalse(
        result.isLessonCompleted(),
        "SQL injection via registration must not compromise control flow when using prepared statements");
  }
}
