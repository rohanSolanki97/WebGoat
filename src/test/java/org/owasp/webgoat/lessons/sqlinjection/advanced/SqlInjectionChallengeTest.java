package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

class SqlInjectionChallengeTest {

  @Test
  @DisplayName("registerNewUser should use PreparedStatement with parameter for username check")
  void registerNewUserUsesPreparedStatementForUserLookup() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkUserStatement = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertUserStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito
        .when(
            connection.prepareStatement(
                "select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkUserStatement);
    Mockito
        .when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertUserStatement);
    Mockito.when(checkUserStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false);

    String username = "user' OR '1'='1";
    String email = "email@example.com";
    String password = "pass";

    AttackResult result = challenge.registerNewUser(username, email, password);

    ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection, times(1)).prepareStatement(queryCaptor.capture());
    assertEquals(
        "select userid from sql_challenge_users where userid = ?",
        queryCaptor.getValue(),
        "User check query should use placeholder for username");

    Mockito.verify(checkUserStatement, times(1)).setString(1, username);
    Mockito.verify(checkUserStatement, times(1)).executeQuery();
    Mockito.verify(connection, never()).createStatement();

    Mockito.verify(insertUserStatement, times(1)).setString(1, username);
    Mockito.verify(insertUserStatement, times(1)).setString(2, email);
    Mockito.verify(insertUserStatement, times(1)).setString(3, password);
  }
}
