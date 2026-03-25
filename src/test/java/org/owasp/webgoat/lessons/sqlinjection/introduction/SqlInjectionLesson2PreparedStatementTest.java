package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;

/**
 * Delta tests for SqlInjectionLesson2 fix:
 * - Uses PreparedStatement with parameter binding instead of executing raw user-controlled SQL.
 */
class SqlInjectionLesson2PreparedStatementTest {

  @Test
  void injectableQuery_bindsUserInputAsParameter_insteadOfExecutingRawSql() throws Exception {
    // Arrange
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement ps = Mockito.mock(PreparedStatement.class);
    ResultSet rs = Mockito.mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(
            eq("SELECT * FROM employees WHERE department = ?"),
            eq(TYPE_SCROLL_INSENSITIVE),
            eq(CONCUR_READ_ONLY)))
        .thenReturn(ps);

    when(ps.executeQuery()).thenReturn(rs);
    when(rs.first()).thenReturn(true);
    when(rs.getString("department")).thenReturn("Sales"); // force failed path; we only care about binding

    SqlInjectionLesson2 sut = new SqlInjectionLesson2(dataSource);

    String attackerInput = "Marketing' OR '1'='1";

    // Act + Assert
    assertDoesNotThrow(() -> sut.injectableQuery(attackerInput));
    verify(ps).setString(1, attackerInput);
    verify(ps).executeQuery();
  }
}
