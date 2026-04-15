package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SqlInjectionChallengeTest {

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid username format")
    void testInvalidUsernameThrowsException() throws Exception {
        SqlInjectionChallenge challenge = new SqlInjectionChallenge();
        challenge.dataSource = mock(DataSource.class);

        assertThrows(IllegalArgumentException.class, () -> {
            challenge.executeChallenge("admin' OR '1'='1");
        });
    }

    @Test
    @DisplayName("Should use PreparedStatement with parameter binding for valid username")
    void testPreparedStatementUsage() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        SqlInjectionChallenge challenge = new SqlInjectionChallenge();
        challenge.dataSource = ds;

        challenge.executeChallenge("validUser");

        verify(conn).prepareStatement("SELECT * FROM accounts WHERE username = ?");
        verify(ps).setString(1, "validUser");
    }
}