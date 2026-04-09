package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.lessons.challenges.Flags;

/*
 * Resolved test path:
 * src/test/java/org/owasp/webgoat/lessons/challenges/challenge5/Assignment5Test.java
 */
public class Assignment5Test {

  @Test
  @DisplayName("login uses parameterized query and binds username and password via setString")
  void login_usesParameterizedQueryWithBoundParameters() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Flags flags = mock(Flags.class);
    Assignment5 assignment = new Assignment5(dataSource, flags);

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);

    String username = "Larry";
    String password = "s3cret";

    org.mockito.Mockito.when(dataSource.getConnection()).thenReturn(connection);
    org.mockito.Mockito.when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    org.mockito.Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    org.mockito.Mockito.when(resultSet.next()).thenReturn(true);

    // Act
    assignment.login(username, password);

    // Assert
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection, times(1)).prepareStatement(sqlCaptor.capture());
    String sql = sqlCaptor.getValue();

    assertTrue(
        sql.contains("userid = ?") && sql.contains("password = ?"),
        "SQL must use placeholders for userid and password instead of concatenating user input");

    verify(preparedStatement).setString(1, username);
    verify(preparedStatement).setString(2, password);
  }
}
