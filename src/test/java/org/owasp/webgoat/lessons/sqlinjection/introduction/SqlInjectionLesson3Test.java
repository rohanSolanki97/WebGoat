package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Test file path (derived):
 * src/test/java/org/owasp/webgoat/lessons/sqlinjection/introduction/SqlInjectionLesson3Test.java
 *
 * Delta tests for SqlInjectionLesson3 focusing on:
 * - restriction of the user query to a specific UPDATE pattern,
 * - use of PreparedStatement rather than executing arbitrary SQL from user input.
 */
class SqlInjectionLesson3Test {

  @Test
  void injectableQuery_rejectsArbitrarySql() {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    String arbitraryQuery = "DROP TABLE employees;";

    AttackResult result = lesson.injectableQuery(arbitraryQuery);

    assertFalse(
        result.getLessonCompleted(),
        "Arbitrary SQL must not be accepted or cause lesson completion after the fix");
  }

  @Test
  void injectableQuery_usesPreparedStatementForAllowedUpdate() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    Statement checkStatement = Mockito.mock(Statement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
    Mockito
        .when(connection.createStatement(
            Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
            Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(checkStatement);
    Mockito.when(checkStatement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
    Mockito.when(resultSet.first()).thenReturn(true);
    Mockito.when(resultSet.getString("department")).thenReturn("Sales");

    String allowedQuery =
        "UPDATE employees SET department = 'Sales' WHERE last_name = 'Barnett'";

    AttackResult result = lesson.injectableQuery(allowedQuery);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();
    org.junit.jupiter.api.Assertions.assertEquals(
        "UPDATE employees SET department = ? WHERE last_name = 'Barnett'",
        usedSql,
        "Fixed code must use a parameterized UPDATE statement");

    Mockito.verify(preparedStatement).setString(1, "Sales");
    Mockito.verify(preparedStatement).executeUpdate();

    assertTrue(
        result.getLessonCompleted(),
        "Lesson should still be completable when using the allowed, parameterized UPDATE");
  }
}
