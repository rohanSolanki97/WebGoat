package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson3 focusing on the changed behavior:
 * - The user-controlled 'query' is no longer executed as arbitrary SQL.
 * - It is now bound as a parameter in a fixed UPDATE statement.
 */
public class SqlInjectionLesson3Test {

  private LessonDataSource dataSource;
  private SqlInjectionLesson3 lesson;
  private Connection connection;
  private PreparedStatement updateStatement;
  private Statement checkStatement;
  private ResultSet resultSet;

  @BeforeEach
  void setUp() throws Exception {
    dataSource = mock(LessonDataSource.class);
    connection = mock(Connection.class);
    updateStatement = mock(PreparedStatement.class);
    checkStatement = mock(Statement.class);
    resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(
            connection.prepareStatement(
                "UPDATE employees SET department = ? WHERE last_name = 'Barnett'"))
        .thenReturn(updateStatement);
    when(connection.createStatement(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY))
        .thenReturn(checkStatement);
    when(checkStatement.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);

    lesson = new SqlInjectionLesson3(dataSource);
  }

  @Test
  void injectableQuery_usesPreparedStatementAndBindsDepartment() throws Exception {
    String newDepartment = "Sales";
    when(resultSet.getString("department")).thenReturn("Sales");

    AttackResult result = lesson.injectableQuery(newDepartment);

    verify(connection)
        .prepareStatement(
            eq("UPDATE employees SET department = ? WHERE last_name = 'Barnett'"));
    verify(updateStatement).setString(1, newDepartment);
    verify(updateStatement).executeUpdate();
    verify(checkStatement)
        .executeQuery("SELECT * FROM employees WHERE last_name='Barnett';");

    assertTrue(
        result.getOutput().contains(newDepartment),
        "Success output should include the user-supplied department value");
  }

  @Test
  void injectableQuery_doesNotTreatInputAsExecutableSql() throws Exception {
    String injected = "Sales'; DROP TABLE employees; --";
    when(resultSet.getString("department")).thenReturn("Sales");

    lesson.injectableQuery(injected);

    verify(connection)
        .prepareStatement(
            eq("UPDATE employees SET department = ? WHERE last_name = 'Barnett'"));
    verify(updateStatement).setString(1, injected);
    verify(updateStatement).executeUpdate();
    verify(checkStatement)
        .executeQuery("SELECT * FROM employees WHERE last_name='Barnett';");
    verifyNoMoreInteractions(updateStatement);
  }
}
