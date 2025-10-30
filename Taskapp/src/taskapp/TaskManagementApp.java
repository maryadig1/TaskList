package taskapp;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.*;

/**
 * Main application class, rewritten as a Swing GUI for a user-friendly
 * experience. Implements a Kanban-style board with three columns and a full
 * login/signup UI.
 */

public class TaskManagementApp {

	private final JFrame frame;
	private final TaskService taskService;
	private UserAuth currentUser = null;

	// GUI Components
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JLabel statusLabel;
	private JPanel mainContentPanel;

	public TaskManagementApp() {
		this.taskService = new TaskService();
		// Initialize Database Tables
		UserAuth.createUsersTable();
		taskService.createTasksTable();
		// Initialize GUI Frame
		frame = new JFrame("Team Task Management System");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 700);
		frame.setLocationRelativeTo(null);
		showLoginUI();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(TaskManagementApp::new);
	}

	// --- UI Helper Methods ---
	private JButton button(String text, ActionListener action) {
		JButton btn = new JButton(text);
		btn.addActionListener(action);
		return btn;
	}

	private JLabel label(String text, Font font, Color color) {
		JLabel lbl = new JLabel(text);
		if (font != null)
			lbl.setFont(font);
		if (color != null)
			lbl.setForeground(color);
		return lbl;
	}

	// --- UI State Management ---
	private void showLoginUI() {
		frame.getContentPane().removeAll();
		frame.setTitle("Login / Register - Team Task Management System");
		JPanel loginPanel = new JPanel(new GridBagLayout());
		loginPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		// Title
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		loginPanel.add(label("Task Manager Login", new Font("Arial", Font.BOLD, 24), null), gbc);
		// Username
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		loginPanel.add(label("Username:", null, null), gbc);
		usernameField = new JTextField(20);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		loginPanel.add(usernameField, gbc);

		// Password
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		loginPanel.add(label("Password:", null, null), gbc);
		passwordField = new JPasswordField(20);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		loginPanel.add(passwordField, gbc);
		// Status Label
		statusLabel = label("Enter credentials.", null, Color.BLUE);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		loginPanel.add(statusLabel, gbc);

		// Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		buttonPanel.add(button("Login", e -> attemptLogin()));
		buttonPanel.add(button("Register", e -> attemptRegister()));
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		loginPanel.add(buttonPanel, gbc);

		frame.add(loginPanel, BorderLayout.CENTER);
		frame.revalidate();
		frame.repaint();
	}

	private void showMainUI() {
		frame.getContentPane().removeAll();
		frame.setTitle("Task Board | User: " + currentUser.getUsername());
		// Top Panel
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(label(" Logged in as: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")",
				new Font("Arial", Font.PLAIN, 14), null), BorderLayout.WEST);
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		toolbar.add(button("Create New Task", e -> showTaskDialog(null)));
		toolbar.add(button("Logout", e -> {
			currentUser = null;
			showLoginUI();
		}));
		topPanel.add(toolbar, BorderLayout.EAST);
		// Main Content Panel
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
		if (currentUser == null)
			return;
		mainContentPanel.removeAll();
		List<Task> allTasks = taskService.getAllTasks();
		// Create task lists
		List<Task> personalTasks = allTasks.stream()
				.filter(t -> t.getAssignedToUserId() == currentUser.getId() && !t.isComplete())
				.collect(Collectors.toList());
		List<Task> incompleteTasks = allTasks.stream().filter(t -> !t.isComplete()).collect(Collectors.toList());
		List<Task> completedTasks = allTasks.stream().filter(Task::isComplete).collect(Collectors.toList());
		// Create columns
		mainContentPanel.add(
				createTaskColumn("My Tasks (" + personalTasks.size() + ")", personalTasks, new Color(255, 230, 230)));
		mainContentPanel.add(createTaskColumn("All Active Tasks (" + incompleteTasks.size() + ")", incompleteTasks,
				new Color(230, 240, 255)));
		mainContentPanel.add(createTaskColumn("Completed Tasks (" + completedTasks.size() + ")", completedTasks,
				new Color(230, 255, 230)));

		mainContentPanel.revalidate();
		mainContentPanel.repaint();
	}

	private JComponent createTaskColumn(String title, List<Task> tasks, Color bgColor) {
		JPanel column = new JPanel();
		column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
		List<Task> sortedTasks = taskService.sortTasksByPriority(tasks);
		column.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		column.setBackground(bgColor);

		JScrollPane scrollPane = new JScrollPane(column);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		sortedTasks.forEach(task -> column.add(createTaskCard(task)));
		column.add(Box.createVerticalGlue());

		return scrollPane;
	}

	private JPanel createTaskCard(Task task) {
		JPanel card = new JPanel(new BorderLayout(5, 5));
		// Border color based on task state
		Color borderColor = task.isComplete() ? new Color(0, 150, 0)
				: task.getPriority().equals("High") ? new Color(200, 0, 0)
						: task.getPriority().equals("Medium") ? new Color(255, 200, 0) : new Color(100, 100, 100);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, 2),
				BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		card.setBackground(Color.WHITE);
		// Header
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setOpaque(false);
		headerPanel.add(label(task.getTitle(), new Font("Arial", Font.BOLD, 14), null), BorderLayout.WEST);
		String statusText = task.isComplete() ? "DONE" : task.getProgress() + "%";
		Color statusColor = task.isComplete() ? Color.GREEN.darker()
				: task.getProgress() < 100 ? Color.BLUE.darker() : Color.GRAY;
		headerPanel.add(label(statusText, new Font("Arial", Font.BOLD, 12), statusColor), BorderLayout.EAST);
		// Details
		JTextArea descriptionPreview = new JTextArea(
				task.getDescription().length() > 50 ? task.getDescription().substring(0, 47) + "..."
						: task.getDescription());
		descriptionPreview.setWrapStyleWord(true);
		descriptionPreview.setLineWrap(true);
		descriptionPreview.setEditable(false);
		descriptionPreview.setOpaque(false);
		descriptionPreview.setBorder(null);

		JPanel detailPanel = new JPanel(new BorderLayout(0, 3));
		detailPanel.setOpaque(false);
		detailPanel.add(descriptionPreview, BorderLayout.CENTER);
		detailPanel.add(label("Assigned: " + task.getAssignedToUsername(), new Font("Arial", Font.ITALIC, 11), null),
				BorderLayout.SOUTH);
		// Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		buttonPanel.add(button("Edit", e -> showTaskDialog(task)));
		buttonPanel.add(button("Delete", e -> attemptDelete(task.getId())));
		card.add(headerPanel, BorderLayout.NORTH);
		card.add(detailPanel, BorderLayout.CENTER);
		card.add(buttonPanel, BorderLayout.SOUTH);
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
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

		// Form components
		JTextField titleField = new JTextField(task != null ? task.getTitle() : "");
		JTextArea descriptionArea = new JTextArea(task != null ? task.getDescription() : "", 4, 20);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
		String[] priorities = { "High", "Medium", "Low" };
		JComboBox<String> priorityBox = new JComboBox<>(priorities);
		if (task != null)
			priorityBox.setSelectedItem(task.getPriority());

		// Assignment Dropdown
		List<UserAuth> users = taskService.getAllUsers();
		String[] userOptions = users.stream().map(u -> u.getId() + " - " + u.getUsername()).toArray(String[]::new);
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
		if (task != null)
			completeCheck.setSelected(task.isComplete());

		// Layout form
		int row = 0;
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		formPanel.add(label("Title:", null, null), gbc);
		gbc.gridx = 1;
		gbc.gridy = row++;
		gbc.weightx = 1.0;
		formPanel.add(titleField, gbc);

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		formPanel.add(label("Description:", null, null), gbc);
		gbc.gridx = 1;
		gbc.gridy = row++;
		gbc.weightx = 1.0;
		gbc.ipady = 30;
		formPanel.add(descriptionScroll, gbc);
		gbc.ipady = 0;
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		formPanel.add(label("Priority:", null, null), gbc);
		gbc.gridx = 1;
		gbc.gridy = row++;
		gbc.weightx = 1.0;
		formPanel.add(priorityBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		formPanel.add(label("Assign To:", null, null), gbc);
		gbc.gridx = 1;
		gbc.gridy = row++;
		gbc.weightx = 1.0;
		formPanel.add(assignedToBox, gbc);
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		formPanel.add(label("Progress (%):", null, null), gbc);
		gbc.gridx = 1;
		gbc.gridy = row++;
		gbc.weightx = 1.0;
		formPanel.add(progressSlider, gbc);

		gbc.gridx = 0;
		gbc.gridy = row++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		formPanel.add(completeCheck, gbc);

		// Action Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(button("Cancel", e -> dialog.dispose()));
		buttonPanel.add(button(task == null ? "Create" : "Save Changes", e -> {
			String title = titleField.getText().trim();
			String description = descriptionArea.getText().trim();
			String priority = (String) priorityBox.getSelectedItem();
			String assignedUserString = (String) assignedToBox.getSelectedItem();
			boolean isComplete = completeCheck.isSelected();
			int progress = progressSlider.getValue();

			// Validation
			if (title.isEmpty() || assignedUserString == null) {
				JOptionPane.showMessageDialog(frame, "Title and assignment must be selected.", "Validation Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			int assignedUserId;
			try {
				assignedUserId = Integer.parseInt(assignedUserString.split(" - ")[0]);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(frame, "Invalid User ID format.", "Validation Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			boolean success;
			if (task == null) {
				success = taskService.createTask(title, description, priority, assignedUserId);
			} else {
				success = taskService.editTask(task.getId(), title, description, priority, assignedUserId, isComplete,
						progress);
			}

			if (success) {
				refreshTaskBoard();
				dialog.dispose();
			} else {
				JOptionPane.showMessageDialog(frame, "Database operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}));

		dialog.add(formPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	private void attemptDelete(int taskId) {
		int dialogResult = JOptionPane.showConfirmDialog(frame,
				"Are you sure you want to delete Task ID " + taskId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			if (taskService.deleteTask(taskId)) {
				refreshTaskBoard();
			} else {
				JOptionPane.showMessageDialog(frame, "Failed to delete task.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}