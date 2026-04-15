package org.owasp.webgoat.lessons.challenges.challenge5;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Assignment5Test {

    @Test
    @DisplayName("Should throw IllegalArgumentException for non-numeric userId")
    void testInvalidUserIdThrowsException() throws Exception {
        Assignment5 assignment = new Assignment5();
        assignment.dataSource = mock(DataSource.class);

        assertThrows(IllegalArgumentException.class, () -> {
            assignment.executeChallenge("abc123");
        });
    }

    @Test
    @DisplayName("Should use PreparedStatement with parameter binding for numeric userId")
    void testPreparedStatementUsage() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        Assignment5 assignment = new Assignment5();
        assignment.dataSource = ds;

        assignment.executeChallenge("42");

        verify(conn).prepareStatement("SELECT * FROM users WHERE id = ?");
        verify(ps).setInt(1, 42);
    }
}