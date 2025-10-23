package tester;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import taskapp.Task;
import taskapp.TaskService;
import taskapp.UserAuth;

class TaskServiceTest {

	private TaskService taskService;

	@BeforeEach
	void setup() {
		taskService = new TaskService();
	}

	// TEST FOR CREATING TASKS TABLE
	@Test
	void testCreateTasksTable() {
		// CALL METHOD TO CREATE TASKS TABLE
		taskService.createTasksTable();

		// CHECK IF TABLE EXISTS
		try (Connection conn = UserAuth.getConnection(); Statement state = conn.createStatement()) {
			var rs = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='tasks';");
			assertTrue(rs.next(), "Table should exist.");
		} catch (Exception e) {
			fail("Table create failed." + e.getMessage());
		}

	}

	// TEST FOR CREATING A TASK
	@Test
	void testCreateTask() {
		// INITIALIZE TASK DETAILS
		String title = "Test Task";
		String description = "Test Description";
		String priority = "High";
		int assignedToUserId = 1;

		// CALL METHOD TO CREATE TASK
		boolean result = taskService.createTask(title, description, priority, assignedToUserId);

		assertTrue(result, "Task should be created successfully.");

	}

	// TEST FOR EDITING A TASK
	@Test
	void testEditTask() {
		// INITIALIZE TASK DETAILS
		String title = "Test Task";
		String description = "Test Description";
		String priority = "High";
		int assignedToUserId = 1;
		taskService.createTask(title, description, priority, assignedToUserId);

		// EDITED TASK DETAILS
		int taskId = 1;
		String newTitle = "Updated Task";
		String newDescription = "Updated Description";
		String newPriority = "Medium";
		int newAssignedToUserId = 2;
		boolean isComplete = true;
		int progress = 75;

		// CALL METHOD TO EDIT TASK
		boolean result = taskService.editTask(taskId, newTitle, newDescription, newPriority, newAssignedToUserId,
				isComplete, progress);

		assertTrue(result, "Task should be edited.");
	}

	// TEST FOR DELETING A TASK
	@Test
	void testDeleteTask() {
		// INITIALIZE TASK DETAILS
		String title = "Test Task to delte";
		String description = "Test Description to delete";
		String priority = "High";
		int assignedToUserId = 1;
		taskService.createTask(title, description, priority, assignedToUserId);

		// CALL METHOD TO DELETE TASK
		int taskId = 1;
		boolean result = taskService.deleteTask(taskId);

		assertTrue(result, "Task should be deleted.");
	}

	// TEST FOR GETTING ALL TASKS
	@Test
	void testGetAllTasks() {
		// INITIALIZE MORE THAN ONE TASK
		taskService.createTask("Task 1", "Description 1", "Low", 1);
		taskService.createTask("Task 2", "Description 2", "Medium", 1);

		List<Task> tasks = taskService.getAllTasks();

		assertNotNull(tasks, "Tasks list should not be null.");
		assertTrue(tasks.size() >= 2, "Should be at least 2 tasks.");
	}

	// TEST FOR SORTING TASKS BY PRIORITY
	@Test
	void tesSortTasksByPriortity() {
		// INITIALIZE TASKS WITH DIFFERENT PRIORITIES
		taskService.createTask("Low Priority Task", "Description", "Low", 1);
		taskService.createTask("Medium Priority Task", "Description", "Medium", 1);
		taskService.createTask("High Priority Task", "Description", "High", 1);

		List<Task> tasks = taskService.getAllTasks();
		List<Task> sortedTasks = taskService.sortTasksByPriority(tasks);

		assertEquals("High", sortedTasks.get(0).getPriority(), "First task should be High Priorirty.");
		assertEquals("Medium", sortedTasks.get(1).getPriority(), "Second task should be Medium Priorirty.");
		assertEquals("Low", sortedTasks.get(2).getPriority(), "Third task should be Low Priorirty.");
	}

	// TEST FOR SORTING TASKS BY USER
	@Test
	void testSortTasksByUser() {
		// INITIALIZE TASKS WITH DIFFERENT USERS
		taskService.createTask("Task 1", "Description", "Low", 1);
		taskService.createTask("Task 2", "Description", "Medium", 2);
		taskService.createTask("Task 3", "Description", "High", 1);

		List<Task> tasks = taskService.getAllTasks();
		List<Task> sortedTasks = taskService.sortTasksByAssignedUser(tasks);

		assertEquals("User 1", sortedTasks.get(0).getAssignedToUsername(), "First task should be assigned to user 1.");
		assertEquals("User 1", sortedTasks.get(1).getAssignedToUsername(), "Second task should be assigned to user 1.");
		assertEquals("User 2", sortedTasks.get(2).getAssignedToUsername(), "Third task should be assigned to user 2.");
	}

}
