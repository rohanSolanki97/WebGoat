package org.owasp.webgoat.lessons.sqlinjection.introduction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;

class SqlInjectionLesson2Test {

  @Test
  void injectableQuery_usesPreparedStatementAndBindsUserInput_insteadOfExecutingRawSql() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString(), anyInt(), anyInt())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);

    when(rs.first()).thenReturn(true);
    when(rs.getString("department")).thenReturn("NotMarketing");

    SqlInjectionLesson2 sut = new SqlInjectionLesson2(dataSource);

    String attackerInput = "Marketing' OR '1'='1";

    // Act
    sut.injectableQuery(attackerInput);

    // Assert
    verify(connection, never()).createStatement(anyInt(), anyInt());
    verify(connection).prepareStatement(eq("SELECT * FROM employees WHERE department = ?"), anyInt(), anyInt());
    verify(ps).setString(1, attackerInput);
    verify(ps).executeQuery();
  }
}
