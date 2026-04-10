package org.owasp.webgoat.lessons.challenges.challenge5;

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
import org.owasp.webgoat.lessons.challenges.Flags;

class Assignment5Test {

  @Test
  void login_usesParameterizedQueryForUserAndPassword() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Flags flags = Mockito.mock(Flags.class);
    Assignment5 assignment = new Assignment5(dataSource, flags);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(flags.getFlag(5)).thenReturn("FLAG-5");

    String username = "Larry";
    String maliciousPassword = "' OR '1'='1";

    assignment.login(username, maliciousPassword);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String sql = sqlCaptor.getValue();

    assertTrue(
        sql.contains("userid = ?") && sql.contains("password = ?"),
        "SQL must use parameter placeholders for username and password");

    verify(preparedStatement).setString(1, username);
    verify(preparedStatement).setString(2, maliciousPassword);
  }
}
