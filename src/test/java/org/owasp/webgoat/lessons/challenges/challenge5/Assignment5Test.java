// File: src/test/java/org/owasp/webgoat/lessons/challenges/challenge5/Assignment5Test.java
package org.owasp.webgoat.lessons.challenges.challenge5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.lessons.challenges.Flags;
import org.owasp.webgoat.container.assignments.AttackResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Delta tests for Assignment5 focusing on the change from a concatenated SQL query
 * to a parameterized PreparedStatement to prevent SQL injection.
 */
public class Assignment5Test {

    private LessonDataSource dataSource;
    private Flags flags;
    private Assignment5 assignment5;

    @BeforeEach
    void setUp() {
        dataSource = mock(LessonDataSource.class);
        flags = mock(Flags.class);
        assignment5 = new Assignment5(dataSource, flags);
    }

    @Test
    void login_usesPreparedStatementWithParameters_preventsSqlInjection() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String maliciousInput = "Larry' OR '1'='1";
        AttackResult result = assignment5.login(maliciousInput, maliciousInput);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sqlCaptor.capture());
        String usedSql = sqlCaptor.getValue();

        assertEquals(
            "select password from challenge_users where userid = ? and password = ?",
            usedSql,
            "SQL must use parameter placeholders, not concatenated input");

        verify(preparedStatement).setString(1, maliciousInput);
        verify(preparedStatement).setString(2, maliciousInput);

        assertNotNull(result, "AttackResult should not be null");
    }

    @Test
    void login_successfulAuthenticationStillWorksWithParameterizedQuery() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(flags.getFlag(5)).thenReturn("FLAG-5");

        AttackResult result = assignment5.login("Larry", "password123");

        assertNotNull(result);
        assertTrue(result.getLessonCompleted(), "Successful login should still be possible");
    }
}
