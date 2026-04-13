package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson3 focusing on the removal of direct execution
 * of user-supplied SQL queries. The updated code executes only a safe dummy
 * query and returns a specific failure feedback.
 */
public class SqlInjectionLesson3Test {

  @Test
  @DisplayName("injectableQuery blocks arbitrary SQL and returns specific feedback")
  void injectableQuery_blocksArbitrarySql() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    Statement stmt = Mockito.mock(Statement.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement(Mockito.anyInt(), Mockito.anyInt())).thenReturn(stmt);
    when(stmt.executeQuery("SELECT 1")).thenReturn(Mockito.mock(ResultSet.class));

    SqlInjectionLesson3 lesson = new SqlInjectionLesson3(dataSource);

    String maliciousQuery = "UPDATE employees SET department='Sales' WHERE last_name='Barnett';";
    AttackResult result = lesson.injectableQuery(maliciousQuery);

    // The updated implementation must NOT execute the user-supplied SQL and instead
    // run the safe dummy query.
    Mockito.verify(stmt).executeQuery("SELECT 1");
    Mockito.verify(stmt, Mockito.never()).executeUpdate(Mockito.anyString());

    assertEquals(
        "sql-injection.arbitrary-query-blocked",
        result.getFeedbackId(),
        "Arbitrary SQL should be blocked with explicit feedback");
  }
}
