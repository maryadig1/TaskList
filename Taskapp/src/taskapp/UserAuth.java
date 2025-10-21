package taskapp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles user data, login/signup, and database connection management.
 */
public class UserAuth {
    
    private static final String JDBC_URL = "jdbc:sqlite:task_manager.db";

    private final int id;
    private final String username;

    // Constructor for fetched user
    public UserAuth(int id, String username) {
        this.id = id;
        this.username = username;
    }

    // Constructor for assignment utility in TaskService (only needs ID/Username)
    public UserAuth(int id, String username, String password) {
        this(id, username);
    }
    
    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }

    /**
     * Gets a connection to the SQLite database.
     */
    public static Connection getConnection() throws SQLException {
        // Register the JDBC driver if not already registered (optional for modern Java)
        // try { Class.forName("org.sqlite.JDBC"); } catch (ClassNotFoundException e) { e.printStackTrace(); }
        return DriverManager.getConnection(JDBC_URL);
    }

    /**
     * Creates the users table if it does not exist.
     */
    public static void createUsersTable() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + "username TEXT NOT NULL UNIQUE,"
                                + "password TEXT NOT NULL" // Simple password storage for demonstration
                                + ");";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(createUsersTable)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }

    /**
     * Registers a new user.
     */
    public static Optional<UserAuth> registerUser(String username, String password) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            System.err.println("Username and password cannot be empty.");
            return Optional.empty();
        }

        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
        
        try (Connection conn = getConnection()) {
            // 1. Check if user exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("Registration failed: Username already exists.");
                    return Optional.empty();
                }
            }
            
            // 2. Insert new user
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password); // In a real app, hash this!
                insertStmt.executeUpdate();
                
                // 3. Retrieve generated ID
                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return Optional.of(new UserAuth(keys.getInt(1), username));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Authenticates a user.
     */
    public static Optional<UserAuth> loginUser(String username, String password) {
        String sql = "SELECT id, username FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new UserAuth(rs.getInt("id"), rs.getString("username")));
                } else {
                    System.err.println("Login failed: Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return Optional.empty();
    }
}
