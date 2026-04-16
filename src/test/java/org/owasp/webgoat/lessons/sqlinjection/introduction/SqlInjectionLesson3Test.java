package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson3 focusing on the fix:
 * - No longer executing user-supplied SQL directly.
 * - Department value is bound as a parameter via PreparedStatement.
 */
public class SqlInjectionLesson3Test {

  @Test
  void injectableQuery_usesPreparedStatementForDepartmentUpdate() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement updateStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement checkStmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("UPDATE employees SET department = ? WHERE last_name='Barnett'"))
        .thenReturn(updateStmt);
    when(connection.createStatement(
            java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStmt);
    when(checkStmt.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Sales");

    String newDepartment = "Sales";

    // Act
    AttackResult result = lesson.injectableQuery(newDepartment);

    // Assert: verify parameterized update is used
    verify(updateStmt).setString(1, newDepartment);
    verify(updateStmt).executeUpdate();

    // Lesson should be marked as completed when department is "Sales"
    assertEquals(true, result.getLessonCompleted());
  }

  @Test
  void injectableQuery_doesNotCompleteLessonWhenDepartmentIsNotSales() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement updateStmt = Mockito.mock(PreparedStatement.class);
    PreparedStatement checkStmt = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("UPDATE employees SET department = ? WHERE last_name='Barnett'"))
        .thenReturn(updateStmt);
    when(connection.createStatement(
            java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStmt);
    when(checkStmt.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Engineering");

    String newDepartment = "Engineering";

    // Act
    AttackResult result = lesson.injectableQuery(newDepartment);

    // Assert
    verify(updateStmt).setString(1, newDepartment);
    verify(updateStmt).executeUpdate();
    assertFalse(result.getLessonCompleted(), "Lesson must not complete for non-Sales department");
  }
}
