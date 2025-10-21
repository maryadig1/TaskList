package taskapp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Main application class, rewritten as a Swing GUI for a user-friendly experience.
 * Implements a Kanban-style board with three columns and a full login/signup UI.
 */
public class TaskManagementApp {

    private JFrame frame;
    private TaskService taskService;
    private UserAuth currentUser = null;

    // GUI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JPanel mainContentPanel; // Holds the 3 task columns

    public TaskManagementApp() {
        this.taskService = new TaskService();
        
        // 1. Initialize Database Tables
        UserAuth.createUsersTable();
        taskService.createTasksTable();
        
        // 2. Initialize GUI Frame
        frame = new JFrame("Team Task Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        
        // Start by showing the login screen
        showLoginUI();
        
        frame.setVisible(true);
    }

    /**
     * The main entry point for the Java application.
     */
    public static void main(String[] args) {
        // Ensure GUI runs on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(TaskManagementApp::new);
    }

    // --- UI State Management ---

    private void showLoginUI() {
        frame.getContentPane().removeAll();
        frame.setTitle("Login / Register - Team Task Management System");
        
        // Use a clean, central panel for authentication
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Task Manager Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 1; loginPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);
        
        // Status Label
        statusLabel = new JLabel("Enter credentials.");
        statusLabel.setForeground(Color.BLUE);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(statusLabel, gbc);

        // Buttons Panel (Clickable buttons)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        
        // Action Listeners
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> attemptRegister());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);

        frame.add(loginPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
    
    private void showMainUI() {
        frame.getContentPane().removeAll();
        frame.setTitle("Task Board | User: " + currentUser.getUsername());
        
        // Top Panel (Toolbar)
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel("  Logged in as: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton createButton = new JButton("Create New Task");
        createButton.addActionListener(e -> showTaskDialog(null));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            currentUser = null;
            showLoginUI();
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.add(createButton);
        toolbar.add(logoutButton);
        
        topPanel.add(userLabel, BorderLayout.WEST);
        topPanel.add(toolbar, BorderLayout.EAST);
        
        // Main Content Panel (3 Columns)
        mainContentPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(mainContentPanel, BorderLayout.CENTER);
        
        refreshTaskBoard();
        
        frame.revalidate();
        frame.repaint();
    }

    // --- Authentication Handlers ---

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        Optional<UserAuth> user = UserAuth.loginUser(username, password);
        if (user.isPresent()) {
            currentUser = user.get();
            showMainUI();
        } else {
            statusLabel.setText("Login failed: Invalid credentials.");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        Optional<UserAuth> user = UserAuth.registerUser(username, password);
        if (user.isPresent()) {
            currentUser = user.get();
            showMainUI();
        } else {
            statusLabel.setText("Registration failed (Username taken or empty).");
            statusLabel.setForeground(Color.RED);
        }
    }

    // --- Task Board Logic ---

    private void refreshTaskBoard() {
        if (currentUser == null) return;
        
        mainContentPanel.removeAll();
        List<Task> allTasks = taskService.getAllTasks();
        
        // Filter tasks for the three requested columns:
        
        // 1. My Personal Tasks (Assigned to current user AND not complete)
        List<Task> personalTasks = allTasks.stream()
                .filter(t -> t.getAssignedToUserId() == currentUser.getId() && !t.isComplete())
                .collect(Collectors.toList());
        
        // 2. All Tasks (Not completed)
        List<Task> incompleteTasks = allTasks.stream()
                .filter(t -> !t.isComplete())
                .collect(Collectors.toList());
                
        // 3. All Completed Tasks
        List<Task> completedTasks = allTasks.stream()
                .filter(Task::isComplete)
                .collect(Collectors.toList());

        // Column 1: My Tasks (Incomplete)
        mainContentPanel.add(createTaskColumn("My Tasks (" + personalTasks.size() + ")", personalTasks, new Color(255, 230, 230)));
        
        // Column 2: All Active Tasks ("In Progress" or Pending)
        mainContentPanel.add(createTaskColumn("All Active Tasks (" + incompleteTasks.size() + ")", incompleteTasks, new Color(230, 240, 255)));
        
        // Column 3: All Completed Tasks
        mainContentPanel.add(createTaskColumn("Completed Tasks (" + completedTasks.size() + ")", completedTasks, new Color(230, 255, 230)));

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private JComponent createTaskColumn(String title, List<Task> tasks, Color bgColor) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        // Use sorting logic from TaskService for display (sort by priority)
        List<Task> sortedTasks = taskService.sortTasksByPriority(tasks);
        
        column.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title), 
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        column.setBackground(bgColor);

        JScrollPane scrollPane = new JScrollPane(column);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        sortedTasks.forEach(task -> column.add(createTaskCard(task)));
        
        // Add a vertical strut to push cards to the top
        column.add(Box.createVerticalGlue());

        return scrollPane;
    }
    
    private JPanel createTaskCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        
        Color borderColor = task.isComplete() ? new Color(0, 150, 0) : 
                            task.getPriority().equals("High") ? new Color(200, 0, 0) : 
                            new Color(100, 100, 100);
                            
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 2),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setBackground(Color.WHITE);
        
        // --- NORTH: Title, Priority, Status ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Status/Progress label
        String statusText = task.isComplete() ? "DONE" : task.getProgress() + "%";
        Color statusColor = task.isComplete() ? Color.GREEN.darker() : 
                            task.getProgress() < 100 ? Color.BLUE.darker() : Color.GRAY;
        
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setForeground(statusColor);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        // --- CENTER: Details (Description, Assigned User) ---
        JTextArea descriptionPreview = new JTextArea(task.getDescription().length() > 50 ? task.getDescription().substring(0, 47) + "..." : task.getDescription());
        descriptionPreview.setWrapStyleWord(true);
        descriptionPreview.setLineWrap(true);
        descriptionPreview.setEditable(false);
        descriptionPreview.setOpaque(false);
        descriptionPreview.setBorder(null);

        JLabel assignedLabel = new JLabel("Assigned: " + task.getAssignedToUsername());
        assignedLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        
        JPanel detailPanel = new JPanel(new BorderLayout(0, 3));
        detailPanel.setOpaque(false);
        detailPanel.add(descriptionPreview, BorderLayout.CENTER);
        detailPanel.add(assignedLabel, BorderLayout.SOUTH);
        
        // --- SOUTH: Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        
        editButton.addActionListener(e -> showTaskDialog(task));
        deleteButton.addActionListener(e -> attemptDelete(task.getId()));
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(detailPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150)); // Allow width to stretch
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        return card;
    }

    // --- Task Dialog (Create/Edit) ---

    private void showTaskDialog(Task task) {
        JDialog dialog = new JDialog(frame, task == null ? "Create New Task" : "Edit Task ID " + task.getId(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(frame);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Components
        JTextField titleField = new JTextField(task != null ? task.getTitle() : "");
        JTextArea descriptionArea = new JTextArea(task != null ? task.getDescription() : "", 4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        
        String[] priorities = {"High", "Medium", "Low"};
        JComboBox<String> priorityBox = new JComboBox<>(priorities);
        if (task != null) priorityBox.setSelectedItem(task.getPriority());

        // Assignment Dropdown
        List<UserAuth> users = taskService.getAllUsers();
        String[] userOptions = users.stream()
            .map(u -> u.getId() + " - " + u.getUsername())
            .toArray(String[]::new);
        JComboBox<String> assignedToBox = new JComboBox<>(userOptions);
        if (task != null) {
            String currentAssignment = task.getAssignedToUserId() + " - " + task.getAssignedToUsername();
            assignedToBox.setSelectedItem(currentAssignment);
        }
        
        // Progress Field
        JSlider progressSlider = new JSlider(0, 100, task != null ? task.getProgress() : 0);
        progressSlider.setMajorTickSpacing(25);
        progressSlider.setMinorTickSpacing(5);
        progressSlider.setPaintTicks(true);
        progressSlider.setPaintLabels(true);

        // Completion Checkbox
        JCheckBox completeCheck = new JCheckBox("Mark as Complete");
        if (task != null) completeCheck.setSelected(task.isComplete());

        // --- Layout ---
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; gbc.ipady = 30; formPanel.add(descriptionScroll, gbc);
        gbc.ipady = 0; // reset
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(priorityBox, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Assign To:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(assignedToBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Progress (%):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(progressSlider, gbc);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST; formPanel.add(completeCheck, gbc);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(task == null ? "Create" : "Save Changes");
        JButton cancelButton = new JButton("Cancel");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            boolean success = handleSave(task, titleField, descriptionArea, priorityBox, assignedToBox, progressSlider, completeCheck);
            if (success) {
                dialog.dispose();
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private boolean handleSave(Task task, JTextField titleField, JTextArea descriptionArea, JComboBox<String> priorityBox, JComboBox<String> assignedToBox, JSlider progressSlider, JCheckBox completeCheck) {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priority = (String) priorityBox.getSelectedItem();
        String assignedUserString = (String) assignedToBox.getSelectedItem();
        boolean isComplete = completeCheck.isSelected();
        int progress = progressSlider.getValue();
        int assignedUserId;

        // Validation
        if (title.isEmpty() || assignedUserString == null) {
            JOptionPane.showMessageDialog(frame, "Title and assignment must be selected.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            // Extract User ID from "ID - Username" string
            assignedUserId = Integer.parseInt(assignedUserString.split(" - ")[0]);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid User ID format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        boolean success;
        if (task == null) {
            // Create
            success = taskService.createTask(title, description, priority, assignedUserId);
        } else {
            // Edit
            success = taskService.editTask(task.getId(), title, description, priority, assignedUserId, isComplete, progress);
        }

        if (success) {
            refreshTaskBoard();
            return true;
        } else {
            JOptionPane.showMessageDialog(frame, "Database operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void attemptDelete(int taskId) {
        int dialogResult = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Task ID " + taskId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            if (taskService.deleteTask(taskId)) {
                refreshTaskBoard();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to delete task.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
