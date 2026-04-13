package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.lessons.challenges.Flags;

/**
 * Delta tests for Assignment5 focusing on the parameterized SQL query in login().
 */
class Assignment5Test {

  @Test
  void login_withValidCredentials_usesParameterizedQueryAndSucceeds() throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Flags flags = mock(Flags.class);
    Assignment5 assignment = new Assignment5(dataSource, flags);

    Connection connection = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(
            "select password from challenge_users where userid = ? and password = ?"))
        .thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(flags.getFlag(5)).thenReturn("FLAG-5");

    AttackResult result = assignment.login("Larry", "correct-password");

    verify(connection)
        .prepareStatement(
            "select password from challenge_users where userid = ? and password = ?");
    verify(ps).setString(1, "Larry");
    verify(ps).setString(2, "correct-password");
    assertEquals(AttackResult.Status.SUCCESS, result.getStatus());
  }

  @Test
  void login_withSqlInjectionPayload_doesNotBypassAuthentication() throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Flags flags = mock(Flags.class);
    Assignment5 assignment = new Assignment5(dataSource, flags);

    Connection connection = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(
            "select password from challenge_users where userid = ? and password = ?"))
        .thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false);

    String injection = "' OR '1'='1";

    AttackResult result = assignment.login("Larry", injection);

    verify(ps).setString(1, "Larry");
    verify(ps).setString(2, injection);
    assertEquals(AttackResult.Status.FAILURE, result.getStatus());
  }
}
