package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.util.StringUtils;

/**
 * Delta tests for SqlInjectionChallenge focusing on the change from a
 * string-concatenated Statement to a parameterized PreparedStatement for the
 * user lookup query. These tests verify:
 * - The lookup query uses a PreparedStatement with a placeholder for username.
 * - The username is bound using setString, preventing direct SQL injection.
 */
public class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_shouldUsePreparedStatementForUserExistenceCheck() throws SQLException {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    PreparedStatement insertStmt = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    String username = "newuser";
    String email = "user@example.com";
    String password = "secret";

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkStmt);
    when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertStmt);
    when(checkStmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert
    verify(connection)
        .prepareStatement("select userid from sql_challenge_users where userid = ?");
    verify(checkStmt).setString(1, username);
    verify(checkStmt).executeQuery();

    verify(insertStmt).setString(1, username);
    verify(insertStmt).setString(2, email);
    verify(insertStmt).setString(3, password);
    verify(insertStmt).execute();

    assertThat(result).isNotNull();
    assertThat(result.getLessonCompleted()).isFalse(); // registration returns informationMessage
  }

  @Test
  void registerNewUser_shouldReportExistingUserUsingPreparedStatement() throws SQLException {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    String username = "existing";
    String email = "user@example.com";
    String password = "secret";

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert
    verify(connection)
        .prepareStatement("select userid from sql_challenge_users where userid = ?");
    verify(checkStmt).setString(1, username);
    verify(checkStmt).executeQuery();

    // Existing user should cause a failed result (lesson not completed)
    assertThat(result.getLessonCompleted()).isFalse();
  }
}
