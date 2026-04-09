package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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

/*
 * Resolved test path:
 * src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallengeTest.java
 */
public class SqlInjectionChallengeTest {

  @Test
  @DisplayName("registerNewUser uses PreparedStatement with placeholder for username in existence check")
  void registerNewUser_usesPreparedStatementForUserCheck() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkUserStatement = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(anyString()))
        .thenReturn(checkUserStatement) // first call: existence check
        .thenReturn(insertStatement);   // second call: insert

    Mockito.when(checkUserStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false); // user does not exist

    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    String username = "newuser";
    String email = "user@example.com";
    String password = "passw0rd";

    // Act
    AttackResult result = challenge.registerNewUser(username, email, password);

    // Assert
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection, times(2)).prepareStatement(sqlCaptor.capture());

    String firstSql = sqlCaptor.getAllValues().get(0);
    assertTrue(
        firstSql.contains("where userid = ?"),
        "checkUserQuery must use a placeholder for userid instead of concatenating the username");

    Mockito.verify(checkUserStatement).setString(1, username);
    // The method should still succeed in the normal path to preserve behavior.
    org.junit.jupiter.api.Assertions.assertTrue(
        result.getOutput().contains("user.created")
            || result.getFeedback().orElse("").contains("user.created"),
        "Successful registration feedback should still be produced");
  }
}
