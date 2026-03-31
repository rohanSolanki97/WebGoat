package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.owasp.webgoat.lessons.challenges.Flags;

class Assignment5Test {

  @Test
  @DisplayName("login should use parameterized query instead of concatenating user input")
  void loginUsesPreparedStatementParameters() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Flags flags = Mockito.mock(Flags.class);
    Assignment5 assignment = new Assignment5(dataSource, flags);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito
        .when(connection.prepareStatement(
            "select password from challenge_users where userid = ? and password = ?"))
        .thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false);

    String username = "Larry' OR '1'='1";
    String password = "anything";
    assignment.login(username, password);

    ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection, times(1)).prepareStatement(queryCaptor.capture());
    assertEquals(
        "select password from challenge_users where userid = ? and password = ?",
        queryCaptor.getValue(),
        "Query should use placeholders and not inline user input");

    Mockito.verify(preparedStatement, times(1)).setString(1, username);
    Mockito.verify(preparedStatement, times(1)).setString(2, password);

    Mockito.verify(preparedStatement, times(1)).executeQuery();
    Mockito.verify(connection, never()).createStatement();
  }
}
