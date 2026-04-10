package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;

class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_usesParameterizedQueryForUserExistenceCheck() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement selectPs = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertPs = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString()))
        .thenReturn(selectPs)
        .thenReturn(insertPs);
    when(selectPs.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    String maliciousUsername = "user' OR '1'='1";
    challenge.registerNewUser(maliciousUsername, "user@example.com", "password");

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String selectSql = sqlCaptor.getValue();

    assertTrue(
        selectSql.contains("where userid = ?"),
        "User existence check must use a placeholder for username");

    verify(selectPs).setString(1, maliciousUsername);
  }
}
