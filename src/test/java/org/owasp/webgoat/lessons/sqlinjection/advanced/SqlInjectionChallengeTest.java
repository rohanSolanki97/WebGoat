// batch_id: BATCH-004
// status: IN_PROGRESS
// test_file_path: src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallengeTest.java
package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionChallenge focusing on the refactor from a
 * concatenated Statement to a parameterized PreparedStatement for the
 * user existence query.
 */
public class SqlInjectionChallengeTest {

  @Test
  @DisplayName("registerNewUser uses parameterized PreparedStatement for username lookup")
  void registerNewUser_usesParameterizedExistenceCheck() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement selectStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement insertStmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(Mockito.anyString()))
        .thenReturn(selectStmt) // first call for select
        .thenReturn(insertStmt); // second call for insert

    when(selectStmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false); // user does not exist

    SqlInjectionChallenge challenge = new SqlInjectionChallenge(dataSource);

    String username = "user' OR '1'='1";
    String email = "user@example.com";
    String password = "pass";

    AttackResult result = challenge.registerNewUser(username, email, password);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection, Mockito.times(2)).prepareStatement(sqlCaptor.capture());
    String selectSql = sqlCaptor.getAllValues().get(0);
    assertEquals(
        "select userid from sql_challenge_users where userid = ?",
        selectSql,
        "Existence-check SQL must be parameterized");

    Mockito.verify(selectStmt).setString(1, username);

    Mockito.verify(insertStmt).setString(1, username);
    Mockito.verify(insertStmt).setString(2, email);
    Mockito.verify(insertStmt).setString(3, password);

    assertEquals("information", result.getLessonResultStatus().toString().toLowerCase());
  }
}
