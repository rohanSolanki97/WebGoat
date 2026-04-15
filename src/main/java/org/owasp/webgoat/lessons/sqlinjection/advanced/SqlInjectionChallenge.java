package org.owasp.webgoat.lessons.sqlinjection.advanced;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SqlInjectionChallenge {

    @Autowired
    private DataSource dataSource;

    @PostMapping("/sqlinjection/challenge")
    public String executeChallenge(@RequestParam("username") String username) throws SQLException {
        // Validate input: alphanumeric only
        if (!username.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid username format");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM accounts WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // process result
                }
            }
        }
        return "success";
    }
}