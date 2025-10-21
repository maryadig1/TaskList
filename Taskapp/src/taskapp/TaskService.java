package taskapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Handles all task-related database operations (CRUD) and task sorting logic.
 * This class also manages the database setup for tasks.
 * NOTE: Updated all methods to handle the new 'progress' field (0-100).
 */
public class TaskService {

    // --- Database Setup ---

    /**
     * Creates the tasks table if it does not exist.
     * ADDED 'progress' column.
     */
    public void createTasksTable() {
        String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks ("
                                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + "title TEXT NOT NULL,"
                                + "description TEXT,"
                                + "priority TEXT CHECK(priority IN ('High', 'Medium', 'Low')) NOT NULL,"
                                + "assigned_to_user_id INTEGER NOT NULL,"
                                + "is_complete BOOLEAN NOT NULL DEFAULT 0,"
                                + "progress INTEGER NOT NULL DEFAULT 0," // <-- ADDED: Default progress is 0
                                + "FOREIGN KEY (assigned_to_user_id) REFERENCES users(id)"
                                + ");";

        try (Connection conn = UserAuth.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createTasksTable)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating tasks table: " + e.getMessage());
        }
    }

    // --- Utility Methods (Fetching Users for Assignment/Display) ---
    
    /**
     * Fetches all registered users (for task assignment).
     */
    public List<UserAuth> getAllUsers() {
        List<UserAuth> users = new ArrayList<>();
        String sql = "SELECT id, username FROM users";
        try (Connection conn = UserAuth.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                // Assuming UserAuth constructor takes (id, username)
                // This is a common pattern when the password hash is not needed outside authentication.
                // If the UserAuth class has been updated, this may need adjustment. Using ID and Username here.
                users.add(new UserAuth(rs.getInt("id"), rs.getString("username")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }
        return users;
    }

    private Optional<String> getUsernameById(int userId) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = UserAuth.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching username: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    // --- Task CRUD Methods ---

    /**
     * Creates a new task in the database.
     * INCLUDES progress (defaults to 0).
     */
    public boolean createTask(String title, String description, String priority, int assignedToUserId) {
        // ADDED 'progress' to the column list
        String sql = "INSERT INTO tasks (title, description, priority, assigned_to_user_id, progress) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = UserAuth.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, priority);
            pstmt.setInt(4, assignedToUserId);
            // progress is implicitly 0 as defined in the SQL string
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating task: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing task's details.
     * ADDED 'progress' parameter and SQL update.
     */
    public boolean editTask(int taskId, String title, String description, String priority, int assignedToUserId, boolean isComplete, int progress) {
        // ADDED progress = ? to the SET clause
        String sql = "UPDATE tasks SET title = ?, description = ?, priority = ?, assigned_to_user_id = ?, is_complete = ?, progress = ? WHERE id = ?";
        try (Connection conn = UserAuth.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, priority);
            pstmt.setInt(4, assignedToUserId);
            pstmt.setBoolean(5, isComplete);
            pstmt.setInt(6, progress); // <-- NEW: progress value
            pstmt.setInt(7, taskId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error editing task: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a task by ID.
     */
    public boolean deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = UserAuth.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches all tasks.
     * Fetches the 'progress' column and passes it to the Task constructor.
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        // ADDED 'progress' to the SELECT statement
        String sql = "SELECT id, title, description, priority, assigned_to_user_id, is_complete, progress FROM tasks ORDER BY id ASC";
        
        try (Connection conn = UserAuth.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int assignedToUserId = rs.getInt("assigned_to_user_id");
                Optional<String> username = getUsernameById(assignedToUserId);
                
                tasks.add(new Task(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("priority"),
                    assignedToUserId,
                    username.orElse("Unknown"),
                    rs.getBoolean("is_complete"),
                    rs.getInt("progress") // <-- NEW: getting progress from DB
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }

    // --- Task Sorting Logic ---

    /**
     * Sorts a list of tasks by priority (High > Medium > Low).
     */
    public List<Task> sortTasksByPriority(List<Task> tasks) {
        List<Task> sortedTasks = new ArrayList<>(tasks);
        // Custom Comparator for priority
        Collections.sort(sortedTasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                // Higher return value means t2 comes before t1 (descending priority)
                return getPriorityValue(t2.getPriority()) - getPriorityValue(t1.getPriority());
            }

            private int getPriorityValue(String priority) {
                switch (priority.toLowerCase()) {
                    case "high": return 3;
                    case "medium": return 2;
                    case "low": return 1;
                    default: return 0;
                }
            }
        });
        return sortedTasks;
    }

    /**
     * Sorts a list of tasks by the username they are assigned to (Alphabetical).
     */
    public List<Task> sortTasksByAssignedUser(List<Task> tasks) {
        List<Task> sortedTasks = new ArrayList<>(tasks);
        // Uses the natural string ordering of the assigned username
        Collections.sort(sortedTasks, Comparator.comparing(Task::getAssignedToUsername));
        return sortedTasks;
    }
}