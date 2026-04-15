package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.lessons.challenges.Flags;

/**
 * Delta unit tests focusing on the security fixes applied:
 * 1. Use of parameterized PreparedStatement instead of concatenated SQL.
 * 2. Strict username format validation.
 */
public class Assignment5Test {

    @Test
    @DisplayName("Should reject invalid username format before querying database")
    void testInvalidUsernameFormatRejected() throws Exception {
        LessonDataSource mockDataSource = mock(LessonDataSource.class);
        Flags mockFlags = mock(Flags.class);
        Assignment5 assignment = new Assignment5(mockDataSource, mockFlags);

        var result = assignment.login("invalid;name", "password123");
        assertTrue(result.getFeedback().contains("invalid.username.format"),
                "Feedback should indicate invalid username format");
        verifyNoInteractions(mockDataSource);
    }

    @Test
    @DisplayName("Should use parameterized query for valid inputs")
    void testParameterizedQueryUsage() throws Exception {
        LessonDataSource mockDataSource = mock(LessonDataSource.class);
        Flags mockFlags = mock(Flags.class);
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockFlags.getFlag(5)).thenReturn("FLAG-5");

        Assignment5 assignment = new Assignment5(mockDataSource, mockFlags);
        var result = assignment.login("Larry", "securePass");

        assertTrue(result.getFeedback().contains("challenge.solved"),
                "Feedback should indicate challenge solved");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(sqlCaptor.capture());
        String capturedSql = sqlCaptor.getValue();
        assertTrue(capturedSql.contains("?"), "SQL should contain parameter placeholders");
        verify(mockStatement).setString(1, "Larry");
        verify(mockStatement).setString(2, "securePass");
    }

    @Test
    @DisplayName("Username regex should match only allowed formats")
    void testUsernamePattern() {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9_]{3,30}$");
        assertTrue(pattern.matcher("Valid_Name123").matches(), "Valid username should match pattern");
        assertFalse(pattern.matcher("in").matches(), "Too short username should not match pattern");
        assertFalse(pattern.matcher("invalid;name").matches(), "Username with special chars should not match pattern");
    }
}