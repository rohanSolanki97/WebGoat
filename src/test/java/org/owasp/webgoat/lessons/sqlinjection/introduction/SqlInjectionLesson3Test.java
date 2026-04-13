package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Delta tests for SqlInjectionLesson3 focusing on the change from executing arbitrary user-supplied
 * SQL to using a parameterized UPDATE statement.
 */
class SqlInjectionLesson3Test {

  @Test
  void injectableQuery_shouldUsePreparedStatementWithDepartmentParameter() throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson3 = new SqlInjectionLesson3(dataSource);

    Connection connection = mock(Connection.class);
    PreparedStatement updateStmt = mock(PreparedStatement.class);
    java.sql.Statement checkStmt = mock(java.sql.Statement.class);
    ResultSet rs = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(
            "UPDATE employees SET department = ? WHERE last_name = 'Barnett'"))
        .thenReturn(updateStmt);
    when(connection.createStatement(
            java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
            java.sql.ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStmt);
    when(checkStmt.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';"))
        .thenReturn(rs);
    when(rs.first()).thenReturn(true);
    when(rs.getString("department")).thenReturn("Sales");

    String department = "Sales";
    AttackResult result = lesson3.injectableQuery(department);

    // Verify prepared statement usage and parameter binding
    verify(connection)
        .prepareStatement("UPDATE employees SET department = ? WHERE last_name = 'Barnett'");
    verify(updateStmt).setString(1, department);
    verify(updateStmt).executeUpdate();

    // Ensure no arbitrary executeUpdate(query) is called on a Statement
    verify(connection, never()).createStatement();
    org.junit.jupiter.api.Assertions.assertTrue(result.getLessonCompleted());
  }
}
