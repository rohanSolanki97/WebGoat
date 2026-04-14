package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

/**
 * Delta tests for SqlInjectionLesson3 focusing on the change from arbitrary SQL execution of a user
 * controlled query to a parameterized UPDATE statement using departmentValue.
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

    Mockito.when(dataSource.getConnection()).thenReturn(connection);
    Mockito
        .when(connection.prepareStatement(Mockito.anyString()))
        .thenReturn(updateStatement);
    Mockito
        .when(connection.createStatement(
            Mockito.eq(ResultSet.TYPE_SCROLL_INSENSITIVE),
            Mockito.eq(ResultSet.CONCUR_READ_ONLY)))
        .thenReturn(checkStatement);
    Mockito.when(checkStatement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
  }

  @Test
  void injectableQuery_usesPreparedStatementWithSingleParameter() throws Exception {
    // Arrange
    String departmentValue = "Sales";

    Mockito.when(resultSet.first()).thenReturn(true);
    Mockito.when(resultSet.getString("department")).thenReturn("Sales");

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    // Act
    AttackResult result = lesson3.injectableQuery(departmentValue);

    // Assert: verify the fixed UPDATE statement and that only the department is parameterized
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();
    assertTrue(
        usedSql.contains("UPDATE employees SET department = ?"),
        "Expected a parameterized UPDATE statement with a single placeholder for department");
    assertTrue(
        usedSql.contains("WHERE last_name='Barnett'") && usedSql.contains("first_name='Tobi'"),
        "Expected employee filter to remain fixed and not derived from user input");

    Mockito.verify(updateStatement).setString(1, departmentValue);
    Mockito.verify(updateStatement).executeUpdate();

    assertTrue(
        result.isLessonCompleted(),
        "Lesson should be completed when departmentValue is set to 'Sales'");
  }

  @Test
  void injectableQuery_sqlInjectionPayloadIsTreatedAsLiteralDepartment() throws Exception {
    // Arrange
    String payload = "Sales', department='Admin"; // typical attempt to break out of value context

    Mockito.when(resultSet.first()).thenReturn(true);
    // The department in DB should not change to arbitrary payload; we simulate it remaining non-Sales.
    Mockito.when(resultSet.getString("department")).thenReturn("HR");

    // Act
    AttackResult result = lesson3.injectableQuery(payload);

    // Assert: even with an injection-like payload, the query structure is not compromised
    Mockito.verify(updateStatement).setString(1, payload);
    assertFalse(
        result.isLessonCompleted(),
        "Injection-style payload should not cause unintended department changes or lesson completion");
  }
}
