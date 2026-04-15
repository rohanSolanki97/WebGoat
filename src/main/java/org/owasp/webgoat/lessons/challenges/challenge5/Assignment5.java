package org.owasp.webgoat.lessons.challenges.challenge5;

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
public class Assignment5 {

    @Autowired
    private DataSource dataSource;

    @PostMapping("/challenge5/assignment")
    public String executeChallenge(@RequestParam("userId") String userId) throws SQLException {
        // Validate input: numeric only
        if (!userId.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid userId format");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setInt(1, Integer.parseInt(userId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // process result
                }
            }
        }
        return "success";
    }
}