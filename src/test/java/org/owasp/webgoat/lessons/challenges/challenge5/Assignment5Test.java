package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.owasp.webgoat.lessons.challenges.Flags;

/**
 * Delta tests for Assignment5 focusing on the vulnerability:
 * "SQL query built directly from user input".
 *
 * The tests verify:
 * - A PreparedStatement with parameter placeholders is used for the login query.
 * - User-supplied values are bound via setString, not concatenated into SQL.
 */
class Assignment5Test {

  @Test
  void login_usesParameterizedPreparedStatementAndBindsUserInput() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Flags flags = mock(Flags.class);
    Assignment5 assignment5 = new Assignment5(dataSource, flags);

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(
            eq("select password from challenge_users where userid = ? and password = ?")))
        .thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);

    String username = "Larry";
    String password = "s3cret";

    // Act
    AttackResult result = assignment5.login(username, password);

    // Assert
    // Core delta assertions: verify parameter binding on the PreparedStatement.
    verify(connection)
        .prepareStatement(
            eq("select password from challenge_users where userid = ? and password = ?"));
    verify(preparedStatement).setString(1, username);
    verify(preparedStatement).setString(2, password);
    verify(preparedStatement).executeQuery();

    // Also assert that we still return some AttackResult instance (API unchanged).
    assertSame(
        AttackResult.class, result.getClass(), "login should still return an AttackResult object");
  }

  @Test
  void login_usesPreparedStatementEvenWithSqlInjectionPayload() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Flags flags = mock(Flags.class);
    Assignment5 assignment5 = new Assignment5(dataSource, flags);

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(
            eq("select password from challenge_users where userid = ? and password = ?")))
        .thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    // Payload that would have changed the WHERE clause when concatenated.
    String username = "Larry' OR '1'='1";
    String password = "irrelevant";

    // Act
    assignment5.login(username, password);

    // Assert
    verify(connection)
        .prepareStatement(
            eq("select password from challenge_users where userid = ? and password = ?"));
    // Even with an injection payload, it must be passed as data into placeholders.
    verify(preparedStatement).setString(1, username);
    verify(preparedStatement).setString(2, password);
  }
}
