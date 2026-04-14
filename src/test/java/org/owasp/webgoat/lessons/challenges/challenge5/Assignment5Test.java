package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.lessons.challenges.Flags;

/**
 * File path (derived from main source): src/test/java/org/owasp/webgoat/lessons/challenges/challenge5/Assignment5Test.java
 *
 * Delta tests for Assignment5 focusing on:
 * - use of parameterized PreparedStatement (no string-concatenated SQL),
 * - user input being bound as parameters, so injection payload is not executed as SQL.
 */
class Assignment5Test {

  @Test
  void login_usesParameterizedQueryAndBindsUserInput() throws Exception {
    LessonDataSource lessonDataSource = Mockito.mock(LessonDataSource.class);
    Flags flags = Mockito.mock(Flags.class);
    Assignment5 assignment5 = new Assignment5(lessonDataSource, flags);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(lessonDataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(true);
    Mockito.when(flags.getFlag(5)).thenReturn("FLAG-5");

    AttackResult result = assignment5.login("Larry", "safePassword");

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
    String usedSql = sqlCaptor.getValue();
    assertEquals(
        "select password from challenge_users where userid = ? and password = ?",
        usedSql,
        "SQL must use parameter placeholders, not raw concatenation");

    Mockito.verify(preparedStatement).setString(1, "Larry");
    Mockito.verify(preparedStatement).setString(2, "safePassword");

    assertTrue(result.getLessonCompleted(), "Successful query should still complete the lesson");
  }

  @Test
  void login_treatsInjectionPayloadAsDataNotCode() throws Exception {
    LessonDataSource lessonDataSource = Mockito.mock(LessonDataSource.class);
    Flags flags = Mockito.mock(Flags.class);
    Assignment5 assignment5 = new Assignment5(lessonDataSource, flags);

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(lessonDataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false); // DB has no such row

    String injectionPassword = "' OR '1'='1";

    AttackResult result = assignment5.login("Larry", injectionPassword);

    Mockito.verify(preparedStatement).setString(1, "Larry");
    Mockito.verify(preparedStatement).setString(2, injectionPassword);

    assertFalse(
        result.getLessonCompleted(),
        "Injection payload must not cause authentication success when no matching row exists");
  }
}
