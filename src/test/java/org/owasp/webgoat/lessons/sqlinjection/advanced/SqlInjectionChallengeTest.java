package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

public class SqlInjectionChallengeTest {

  @Test
  public void registerNewUser_usesPreparedStatementForCheck() throws SQLException {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement checkStatement = mock(PreparedStatement.class);
    PreparedStatement insertStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);
    when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertStatement);

    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    String username = "alice' OR '1'='1";
    String email = "alice@example.com";
    String password = "SecureP@ssw0rd";

    AttackResult result = challenge.registerNewUser(username, email, password);

    verify(connection)
        .prepareStatement("select userid from sql_challenge_users where userid = ?");
    verify(checkStatement).setString(1, username);
    verify(checkStatement).executeQuery();

    verify(connection).prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)");
    verify(insertStatement).setString(1, username);
    verify(insertStatement).setString(2, email);
    verify(insertStatement).setString(3, password);
    verify(insertStatement).execute();

    assertThat(result.getLessonCompleted()).isFalse();
  }

  @Test
  public void registerNewUser_userExistsBranch() throws SQLException {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement checkStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);

    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    AttackResult result =
        challenge.registerNewUser("existingUser", "user@example.com", "secret");

    assertThat(result.getLessonCompleted()).isFalse();
    verify(connection, times(1))
        .prepareStatement("select userid from sql_challenge_users where userid = ?");
    verifyNoMoreInteractions(connection);
  }
}
