package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson3 focusing on the change that removed direct execution
 * of user-controlled SQL and replaced it with a fixed, parameterized UPDATE.
 */
public class SqlInjectionLesson3Test {

  @Test
  void injectableQuery_shouldNotExecuteUserSuppliedSqlButUseFixedParameterizedUpdate()
      throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement updateStatement = mock(PreparedStatement.class);
    Statement checkStatement = mock(Statement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("UPDATE employees SET department = ? WHERE last_name = ?"))
        .thenReturn(updateStatement);
    when(connection.createStatement(
            java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Sales");

    String attackerQuery = "UPDATE employees SET salary=999999 WHERE last_name='Barnett';";

    // Act
    AttackResult result = lesson.injectableQuery(attackerQuery);

    // Assert
    // Verify that the user-supplied query string is NOT executed
    verify(connection, never()).createStatement();
    verify(checkStatement, times(1))
        .executeQuery("SELECT * FROM employees WHERE last_name='Barnett';");

    // Verify that the fixed prepared statement is used instead
    verify(connection)
        .prepareStatement("UPDATE employees SET department = ? WHERE last_name = ?");
    verify(updateStatement).setString(1, "Sales");
    verify(updateStatement).setString(2, "Barnett");
    verify(updateStatement).executeUpdate();

    assertThat(result).isNotNull();
    assertThat(result.getLessonCompleted()).isTrue();
  }

  @Test
  void injectableQuery_shouldFailWhenDepartmentIsNotSales() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement updateStatement = mock(PreparedStatement.class);
    Statement checkStatement = mock(Statement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("UPDATE employees SET department = ? WHERE last_name = ?"))
        .thenReturn(updateStatement);
    when(connection.createStatement(
            java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Other");

    String anyQuery = "ignored";

    // Act
    AttackResult result = lesson.injectableQuery(anyQuery);

    // Assert
    verify(connection)
        .prepareStatement("UPDATE employees SET department = ? WHERE last_name = ?");
    assertThat(result).isNotNull();
    assertThat(result.getLessonCompleted()).isFalse();
  }
}
