package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.util.StringUtils;

/**
 * Delta unit tests for SqlInjectionChallenge focusing on the fixed SQL injection vulnerability:
 * - Input validation for null/empty, length, and allowed characters
 * - Parameterized query usage
 * - Correct challenge logic for valid and invalid inputs
 */
public class SqlInjectionChallengeTest {

    @Mock
    private LessonDataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private SqlInjectionChallenge challenge;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        challenge = new SqlInjectionChallenge(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    public void testRejectsEmptyInput() throws Exception {
        AttackResult result = challenge.completed("");
        assertTrue(result.getFeedback().contains("sql-injection.challenge.invalidinput"), "Should reject empty input");
    }

    @Test
    public void testRejectsTooLongInput() throws Exception {
        String longInput = "a".repeat(51);
        AttackResult result = challenge.completed(longInput);
        assertTrue(result.getFeedback().contains("sql-injection.challenge.inputtoolong"), "Should reject overly long input");
    }

    @Test
    public void testRejectsInvalidCharacters() throws Exception {
        AttackResult result = challenge.completed("invalid;DROP TABLE");
        assertTrue(result.getFeedback().contains("sql-injection.challenge.invalidchars"), "Should reject input with invalid characters");
    }

    @Test
    public void testUsesParameterizedQueryForValidInput() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        AttackResult result = challenge.completed("validUser");
        assertTrue(result.getFeedback().contains("sql-injection.challenge.solved"), "Should solve challenge for valid user");

        verify(connection).prepareStatement("SELECT * FROM users WHERE userid = ?");
        verify(preparedStatement).setString(1, "validUser");
    }

    @Test
    public void testFailsChallengeForNonExistingUser() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        AttackResult result = challenge.completed("nonExisting");
        assertTrue(result.getFeedback().contains("sql-injection.challenge.failed"), "Should fail challenge for non-existing user");
    }

    @Test
    public void testDatabaseErrorHandledGracefully() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        AttackResult result = challenge.completed("validUser");
        assertTrue(result.getFeedback().contains("sql-injection.challenge.dberror"), "Should handle database error gracefully");
    }
}