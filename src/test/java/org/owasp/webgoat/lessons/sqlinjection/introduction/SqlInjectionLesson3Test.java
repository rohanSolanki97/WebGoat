package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/*
 * Delta tests for:
 *   Source: src/main/java/org/owasp/webgoat/lessons/sqlinjection/introduction/SqlInjectionLesson3.java
 *   Test:   src/test/java/org/owasp/webgoat/lessons/sqlinjection/introduction/SqlInjectionLesson3Test.java
 *
 * Focus: refactor from executing arbitrary SQL to a single parameterized UPDATE with user input as value only.
 */
public class SqlInjectionLesson3Test {

  private LessonDataSource dataSource;
  private SqlInjectionLesson3 lesson3;
  private Connection connection;
  private PreparedStatement updateStatement;
  private Statement checkStatement;
  private ResultSet resultSet;

  @BeforeEach
  void setup() throws Exception {
    dataSource = Mockito.mock(LessonDataSource.class);
    lesson3 = new SqlInjectionLesson3(dataSource);

    connection = Mockito.mock(Connection.class);
    updateStatement = Mockito.mock(PreparedStatement.class);
    checkStatement = Mockito.mock(Statement.class);
    resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(updateStatement);
    when(connection.createStatement(
            java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery(anyString())).thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Sales");
  }

  @Test
  void completedShouldUseParameterizedUpdateStatement() {
    // Arrange
    String department = "Sales";

    // Act
    AttackResult result = lesson3.completed(department);

    // Assert: SQL text is fixed and user input is bound as parameter
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();

    assertEquals(
        "UPDATE employees SET department = ? WHERE last_name = 'Barnett'",
        usedSql,
        "UPDATE statement must be parameterized and not contain raw user SQL");
    verify(updateStatement).setString(1, department);
    verify(updateStatement).executeUpdate();
  }

  @Test
  void completedShouldTreatMaliciousPayloadAsValueNotSql() {
    // Arrange
    String malicious = "Sales', salary = 9999999 --";

    // Act
    lesson3.completed(malicious);

    // Assert: still same safe SQL, malicious string bound as parameter
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();

    assertEquals(
        "UPDATE employees SET department = ? WHERE last_name = 'Barnett'",
        usedSql,
        "Arbitrary SQL text must not be executed; only bound as parameter");
    verify(updateStatement).setString(1, malicious);
  }
}
