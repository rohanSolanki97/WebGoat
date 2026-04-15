// File: src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallengeTest.java
// Derived from src/main/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallenge.java
package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;

public class SqlInjectionChallengeTest {

  @Test
  void registerNewUser_usesPreparedStatementForUserExistenceCheck() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement checkStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertStmt = Mockito.mock(PreparedStatement.class);
    ResultSet rs = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito
        .when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
        .thenReturn(checkStmt);
    Mockito.when(checkStmt.executeQuery()).thenReturn(rs);
    Mockito.when(rs.next()).thenReturn(false);
    Mockito
        .when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)"))
        .thenReturn(insertStmt);

    String username = "user' OR '1'='1";
    String email = "user@example.com";
    String password = "pass";

    // Act
    challenge.registerNewUser(username, email, password);

    // Assert: verify that a parameterized query is used and the username is bound
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    assertEquals(
        "select userid from sql_challenge_users where userid = ?",
        sqlCaptor.getValue());

    Mockito.verify(checkStmt).setString(1, username);
  }
}
