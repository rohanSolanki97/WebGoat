package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson3 focusing on the parameterized query behavior for accountName.
 */
public class SqlInjectionLesson3Test {

  @Test
  void completed_shouldUsePreparedStatementWithParameterizedAccountName() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false);

    String maliciousAccountName = "1234' OR '1'='1";

    // Act
    AttackResult result = lesson.completed(maliciousAccountName);

    // Assert: verify prepared statement SQL uses placeholder and binding is applied
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();

    assertTrue(
        usedSql.contains("where cc_number = ?"),
        "SQL for account lookup must use a parameter placeholder rather than concatenating user input");

    Mockito.verify(preparedStatement).setString(1, maliciousAccountName);
    assertFalse(
        result.getLessonCompleted(),
        "Lesson must not be completed for a pure injection attempt using parameterized query");
  }
}
