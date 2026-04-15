package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

/**
 * Delta tests for SqlInjectionLesson6b focusing on:
 * - Removal of stack-trace based logging (no behavior to assert directly here).
 * - Introduction of a Secure, HttpOnly cookie in the completed() endpoint.
 *
 * These tests verify that:
 * - completed() sets a cookie with the Secure and HttpOnly flags.
 * - Existing success/failure behavior is preserved.
 */
public class SqlInjectionLesson6bTest {

  @Test
  @DisplayName("completed should set a Secure, HttpOnly cookie on every call")
  void completed_setsSecureHttpOnlyCookie() throws Exception {
    // Arrange
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    Statement statement = mock(Statement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenReturn(statement);
    when(statement.executeQuery("SELECT password FROM user_system_data WHERE user_name = 'dave'"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("password")).thenReturn("secret-password");

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // We capture the cookie that is added to the response
    doAnswer(invocation -> {
          Cookie cookie = invocation.getArgument(0);
          assertThat(cookie.getName()).isEqualTo("session_id");
          assertThat(cookie.isHttpOnly()).isTrue();
          assertThat(cookie.getSecure()).isTrue();
          assertThat(cookie.getPath()).isEqualTo("/");
          return null;
        })
        .when(response)
        .addCookie(any(Cookie.class));

    // Act
    AttackResult result = lesson.completed("secret-password", response);

    // Assert
    verify(response).addCookie(any(Cookie.class));
    assertThat(result.getLessonCompleted()).isTrue();
  }

  @Test
  @DisplayName("completed should still fail when userid_6b does not match password")
  void completed_preservesFailureBehavior() throws Exception {
    LessonDataSource dataSource = mock(LessonDataSource.class);
    Connection connection = mock(Connection.class);
    Statement statement = mock(Statement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
        .thenReturn(statement);
    when(statement.executeQuery("SELECT password FROM user_system_data WHERE user_name = 'dave'"))
        .thenReturn(resultSet);
    when(resultSet.first()).thenReturn(true);
    when(resultSet.getString("password")).thenReturn("secret-password");

    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);
    HttpServletResponse response = mock(HttpServletResponse.class);

    AttackResult result = lesson.completed("wrong", response);

    assertThat(result.getLessonCompleted()).isFalse();
    verify(response).addCookie(any(Cookie.class));
  }
}
