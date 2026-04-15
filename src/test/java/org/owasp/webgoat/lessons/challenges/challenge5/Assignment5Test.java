package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.lessons.challenges.Flags;
import org.springframework.util.StringUtils;

/**
 * Delta tests for Assignment5 focusing on the changed behavior:
 * - SQL is now built using PreparedStatement placeholders instead of string concatenation.
 *
 * These tests verify:
 * - Correct SQL text with '?' placeholders is used.
 * - User inputs are bound via setString calls (no direct concatenation).
 */
public class Assignment5Test {

  private LessonDataSource dataSource;
  private Flags flags;
  private Assignment5 assignment5;
  private Connection connection;
  private PreparedStatement preparedStatement;
  private ResultSet resultSet;

  @BeforeEach
  void setUp() throws Exception {
    dataSource = mock(LessonDataSource.class);
    flags = mock(Flags.class);
    assignment5 = new Assignment5(dataSource, flags);

    connection = mock(Connection.class);
    preparedStatement = mock(PreparedStatement.class);
    resultSet = mock(ResultSet.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);
    when(flags.getFlag(5)).thenReturn("FLAG-5");
  }

  @Test
  void login_usesParameterizedQueryAndBindsUserInputs() throws Exception {
    // Arrange
    String username = "Larry";
    String password = "securePassword";
    when(resultSet.next()).thenReturn(true);

    // Act
    assignment5.login(username, password);

    // Assert
    // Validate that the SQL statement uses placeholders instead of concatenating user input
    verify(connection)
        .prepareStatement(
            eq("select password from challenge_users where userid = ? and password = ?"));
    // Verify that user-supplied values are bound through setString
    verify(preparedStatement).setString(1, username);
    verify(preparedStatement).setString(2, password);
    // Ensure executeQuery is still invoked
    verify(preparedStatement).executeQuery();
  }

  @Test
  void login_doesNotProceedOnEmptyInputs() throws Exception {
    // Arrange
    String username = "";
    String password = "   ";

    // Act
    assignment5.login(username, password);

    // Assert
    // On invalid input, the method must not call into the data source at all
    verifyNoInteractions(connection);
    // Also verify that Spring's StringUtils behaves as expected for whitespace-only input
    assertEquals(false, StringUtils.hasText(password));
  }
}
