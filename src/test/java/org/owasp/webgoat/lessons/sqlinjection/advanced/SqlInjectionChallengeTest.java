package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;

/**
 * Delta test focusing on the change from a Statement with concatenated SQL to a
 * PreparedStatement with a parameterized query for user existence check.
 */
public class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_usesPreparedStatementForUserLookup() throws SQLException {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    String username = "alice";
    String email = "alice@example.com";
    String password = "pwd";

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertStmt = Mockito.mock(PreparedStatement.class);
    ResultSet rs = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(
            connection.prepareStatement(
                "select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkStmt);
    Mockito.when(checkStmt.executeQuery()).thenReturn(rs);
    Mockito.when(rs.next()).thenReturn(false);
    Mockito.when(
            connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertStmt);

    // Act
    challenge.registerNewUser(username, email, password);

    // Assert
    // The secure behavior is using a parameterized query with a bound username.
    verify(checkStmt).setString(eq(1), eq(username));
    verify(checkStmt).executeQuery();
  }
}
