package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.lessons.challenges.Flags;

/*
 * Delta test for:
 *   Source: src/main/java/org/owasp/webgoat/lessons/challenges/challenge5/Assignment5.java
 *   Test:   src/test/java/org/owasp/webgoat/lessons/challenges/challenge5/Assignment5Test.java
 *
 * Focus: change from SQL string concatenation to parameterized PreparedStatement.
 */
public class Assignment5Test {

  private LessonDataSource dataSource;
  private Flags flags;
  private Assignment5 assignment5;
  private Connection connection;
  private PreparedStatement preparedStatement;
  private ResultSet resultSet;

  @BeforeEach
  void setup() throws Exception {
    dataSource = Mockito.mock(LessonDataSource.class);
    flags = Mockito.mock(Flags.class);
    assignment5 = new Assignment5(dataSource, flags);

    connection = Mockito.mock(Connection.class);
    preparedStatement = Mockito.mock(PreparedStatement.class);
    resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);
  }

  @Test
  void loginShouldUseParameterizedQueryAndBindUserInput() throws Exception {
    // Arrange
    String username = "Larry";
    String password = "secret";
    when(resultSet.next()).thenReturn(true);
    when(flags.getFlag(5)).thenReturn("FLAG5");

    // Act
    AttackResult result = assignment5.login(username, password);

    // Assert: SQL must be parameterized and inputs bound as parameters
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();

    String expectedSql =
        "select password from challenge_users where userid = ? and password = ?";
    assertEquals(expectedSql, usedSql, "Login query must use parameter markers");

    verify(preparedStatement).setString(1, username);
    verify(preparedStatement).setString(2, password);
    verify(preparedStatement).executeQuery();
  }

  @Test
  void loginShouldTreatMaliciousInputAsDataNotSql() throws Exception {
    // Arrange: attempt classic injection payload
    String maliciousUsername = "Larry' OR '1'='1";
    String password = "irrelevant";
    when(resultSet.next()).thenReturn(false); // no row when parameterized

    // Act
    AttackResult result = assignment5.login(maliciousUsername, password);

    // Assert: still uses same parameterized query and binds the malicious string as a value
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();

    String expectedSql =
        "select password from challenge_users where userid = ? and password = ?";
    assertEquals(expectedSql, usedSql, "Query must remain parameterized under malicious input");

    verify(preparedStatement).setString(1, maliciousUsername);
    verify(preparedStatement).setString(2, password);
  }
}
