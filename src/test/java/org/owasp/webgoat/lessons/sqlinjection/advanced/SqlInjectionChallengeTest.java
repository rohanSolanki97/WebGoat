// File: src/test/java/org/owasp/webgoat/lessons/sqlinjection/advanced/SqlInjectionChallengeTest.java
package org.owasp.webgoat.lessons.sqlinjection.advanced;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Delta tests for SqlInjectionChallenge focusing on the change from concatenated
 * SQL to a PreparedStatement for the user-existence check query.
 */
public class SqlInjectionChallengeTest {

    private LessonDataSource dataSource;
    private SqlInjectionChallenge challenge;

    @BeforeEach
    void setUp() {
        dataSource = mock(LessonDataSource.class);
        challenge = new SqlInjectionChallenge(dataSource);
    }

    @Test
    void registerNewUser_usesPreparedStatementForUserCheck_preventsInjection() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
                .thenReturn(checkStmt);
        when(connection.prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)")).thenReturn(insertStmt);
        when(checkStmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // user does not exist

        String maliciousUsername = "bob' OR '1'='1";
        AttackResult result =
                challenge.registerNewUser(maliciousUsername, "bob@example.com", "pass");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection, atLeastOnce()).prepareStatement(sqlCaptor.capture());
        assertTrue(
            sqlCaptor.getAllValues().contains("select userid from sql_challenge_users where userid = ?"),
            "User check must use parameterized SQL, not string concatenation");

        verify(checkStmt).setString(1, maliciousUsername);
        verify(checkStmt).executeQuery();

        assertNotNull(result);
    }

    @Test
    void registerNewUser_existingUserStillDetectedWithParameterizedQuery() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("select userid from sql_challenge_users where userid = ?"))
                .thenReturn(checkStmt);
        when(checkStmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true); // user exists

        AttackResult result =
                challenge.registerNewUser("existing", "existing@example.com", "pass");

        assertNotNull(result);
        assertFalse(
            result.getLessonCompleted(),
            "Existing user should not complete the lesson, behavior must be preserved");
    }
}
