// File: src/test/java/org/owasp/webgoat/lessons/sqlinjection/introduction/SqlInjectionLesson3Test.java
package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

public class SqlInjectionLesson3Test {

  @Test
  void injectableQuery_ignoresUserSuppliedQueryAndUsesParameterizedUpdate() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement updateStmt = Mockito.mock(PreparedStatement.class);
    java.sql.Statement checkStmt = Mockito.mock(java.sql.Statement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("UPDATE employees SET department = ? WHERE last_name = ?"))
        .thenReturn(updateStmt);
    when(connection.createStatement(
            java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY))
        .thenReturn(checkStmt);
    when(checkStmt.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("department")).thenReturn("Sales");

    String attackerQuery = "UPDATE employees SET department='IT' WHERE last_name='Barnett';";

    // Act
    AttackResult result = lesson.injectableQuery(attackerQuery);

    // Assert
    // 1) Ensure only the fixed, parameterized update is executed
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String executedSql = sqlCaptor.getValue();
    org.junit.jupiter.api.Assertions.assertEquals(
        "UPDATE employees SET department = ? WHERE last_name = ?", executedSql);

    // 2) Verify attacker-supplied SQL is never sent directly to the database
    Mockito.verify(updateStmt).setString(1, "Sales");
    Mockito.verify(updateStmt).setString(2, "Barnett");

    // 3) Lesson still completes successfully when department is Sales
    assertTrue(result.getLessonCompleted());
  }
}
