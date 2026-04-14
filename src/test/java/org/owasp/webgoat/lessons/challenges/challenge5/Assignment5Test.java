package org.owasp.webgoat.lessons.challenges.challenge5;

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
import org.owasp.webgoat.lessons.challenges.Flags;

/**
 * Delta tests for Assignment5 focusing on the change from string-concatenated SQL to a parameterized
 * PreparedStatement to prevent SQL Injection.
 */
public class Assignment5Test {

  private LessonDataSource dataSource;
  private Flags flags;
  private Assignment5 assignment5;

  private Connection connection;
  private PreparedStatement preparedStatement;
  private ResultSet resultSet;

  @BeforeEach
  void setup() throws Exception {
    dataSource = Mockito.mock(LessonDataSource.class);
    flags = Mockito.mock(Flags.class);
    assignment5 = new Assignment5(dataSource, flags);

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
  void login_usesParameterizedQueryAndBindsUserInputs() throws Exception {
    // Arrange
    Mockito.when(resultSet.next()).thenReturn(true);
    Mockito.when(flags.getFlag(5)).thenReturn("FLAG-5");

    String username = "Larry";
    String password = "' OR '1'='1";

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    // Act
    AttackResult result = assignment5.login(username, password);

    // Assert: ensure the SQL text contains placeholders instead of concatenated user input
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();
    assertTrue(
        usedSql.contains("userid = ?") && usedSql.contains("password = ?"),
        "Expected parameter placeholders in SQL, not raw concatenated input");

    // Assert: ensure both parameters are bound via setString
    Mockito.verify(preparedStatement).setString(1, username);
    Mockito.verify(preparedStatement).setString(2, password);

    // Also ensure a successful path still works when credentials match
    assertTrue(result.isLessonCompleted(), "Expected lesson to be marked completed on valid login");
  }

  @Test
  void login_sqlInjectionPayloadDoesNotAlterQueryStructure() throws Exception {
    // Arrange
    // Simulate no matching row even with a typical SQL injection payload
    Mockito.when(resultSet.next()).thenReturn(false);

    String username = "Larry";
    String password = "' OR '1'='1";

    // Act
    AttackResult result = assignment5.login(username, password);

    // Assert: injection-like password should not succeed when using prepared statements
    assertFalse(
        result.isLessonCompleted(),
        "SQL injection style password must not bypass authentication with prepared statements");
  }
}
