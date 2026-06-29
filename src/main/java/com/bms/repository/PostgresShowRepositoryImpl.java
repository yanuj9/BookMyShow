package com.bms.repository;

import com.bms.domain.Show;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Used for production. Connects to the Docker Postgres instance.
 */
public class PostgresShowRepositoryImpl implements ShowRepository {
    
    private final Connection dbConnection; // Inject via constructor (e.g., HikariCP DataSource)

    public PostgresShowRepositoryImpl(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void save(Show show) {
        // Example JDBC implementation
        String sql = "INSERT INTO shows (id, movie_id, screen_id, start_time) VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET ...";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, show.getShowId());
            stmt.setString(2, show.getMovieId());
            // ... set other parameters and execute
        } catch (Exception e) {
            throw new RuntimeException("Database error saving show", e);
        }
    }

    @Override
    public Show findById(String showId) {
        // Example JDBC implementation
        String sql = "SELECT * FROM shows WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, showId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Map ResultSet back to Show domain object here
                return null; // returning null just for compilation placeholder
            }
        } catch (Exception e) {
            throw new RuntimeException("Database error fetching show", e);
        }
        return null;
    }
}
