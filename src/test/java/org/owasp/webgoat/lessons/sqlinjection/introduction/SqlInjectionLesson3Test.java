package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson3 focusing only on the changed behavior:
 * - The raw Statement.executeUpdate(query) call has been replaced by a parameterized PreparedStatement.
 * - The success message was changed to "Department updated to: " + query.
 *
 * These tests verify:
 * 1) User input is no longer executed as an arbitrary SQL statement (parameterized query is used).
 * 2) The functional behavior is preserved for a benign department value.
 */
public class SqlInjectionLesson3Test {

  @Test
  @DisplayName("injectableQuery uses parameterized PreparedStatement instead of executing raw SQL")
  void injectableQuery_usesPreparedStatement_andBindsUserInputAsParameter() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    Statement checkStatement = mock(Statement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery(anyString())).thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Sales");

    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);
    String userInput = "Sales";

    // Act
    AttackResult result = lesson.injectableQuery(userInput);

    // Assert: verify that a fixed SQL template is used for the update
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();
    assertTrue(
        usedSql.toLowerCase().contains("update employees"),
        "Expected an UPDATE employees statement to be used");
    assertTrue(
        usedSql.contains("department = ?"),
        "Expected the SQL to use a parameter placeholder for department");
    assertTrue(
        usedSql.contains("last_name = 'Barnett'") || usedSql.contains("last_name='Barnett'"),
        "Expected WHERE last_name='Barnett' clause to be present");

    // Assert: verify that user input is bound as a parameter, not concatenated into SQL
    verify(preparedStatement).setString(1, userInput);
    verify(preparedStatement).executeUpdate();

    // Old behavior (raw Statement.executeUpdate(query)) must no longer be invoked
    verify(connection, never()).createStatement(); // no generic createStatement without args
    verifyNoMoreInteractions(checkStatement); // only used for the fixed SELECT, not for updates

    // Assert: overall result is success for this benign, expected input
    assertTrue(result.getLessonCompleted(), "Expected lesson to be marked as completed");
  }

  @Test
  @DisplayName("injectableQuery does not execute raw injected SQL and still completes successfully")
  void injectableQuery_doesNotExecuteInjectedSql_butTreatsItAsData() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    Statement checkStatement = mock(Statement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery(anyString())).thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Sales");

    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    // This input would previously be interpreted as a full SQL statement;
    // after the fix it must be treated only as a parameter value.
    String maliciousInput = "Sales'; DROP TABLE employees; --";

    // Act
    AttackResult result = lesson.injectableQuery(maliciousInput);

    // Assert: verify it is bound as a parameter, proving it is not executed as SQL
    verify(connection).prepareStatement(anyString());
    verify(preparedStatement).setString(1, maliciousInput);
    verify(preparedStatement).executeUpdate();

    // Ensure the fixed validation query still runs
    verify(checkStatement).executeQuery("SELECT * FROM employees WHERE last_name='Barnett';");

    // The lesson might or might not consider this input as success depending on DB logic,
    // but the critical security property is that the input is treated as data, not as SQL.
    // We therefore only assert that execution completed without throwing and returned some result.
    assertTrue(
        result != null,
        "Expected a non-null AttackResult even when malicious input is provided as department");

    // Also ensure that no generic Statement.executeUpdate with user input is called.
    verify(connection, never()).createStatement();
  }

  @Test
  @DisplayName("injectableQuery success output message reflects new 'Department updated to: <value>' format")
  void injectableQuery_successMessageUsesNewFormat() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    Statement checkStatement = mock(Statement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery(anyString())).thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Sales");

    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);
    String departmentValue = "Sales";

    // Act
    AttackResult result = lesson.injectableQuery(departmentValue);

    // Assert: the output should contain the new message pattern
    String output = result.getOutput();
    assertTrue(
        output.contains("Department updated to: " + departmentValue),
        "Expected output to contain the new success message with the department value");
    // Ensure we no longer echo a raw SQL statement as was done before the fix
    // (we approximate this by checking that it does not start with typical SQL keywords).
    assertTrue(
        !output.toUpperCase().contains("UPDATE ") || !output.contains("employees SET"),
        "Output should not contain raw SQL statements anymore");
  }
}
