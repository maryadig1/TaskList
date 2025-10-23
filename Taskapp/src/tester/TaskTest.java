package tester;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import taskapp.Task;

class TaskTest {

	// TEST FOR CONSTRUCTOR AND GETTERS
	@Test
	void testConstructorAndGetters() {
		Task task = new Task(1, "Test Task", "This is a test task.", "High", 42, "doubtfire", false, 50);

		assertEquals(1, task.getId(), "Task ID should match.");
		assertEquals("Test Task", task.getTitle(), "Task title should match.");
		assertEquals("This is a test task.", task.getDescription(), "Task Description should match.");
		assertEquals("High", task.getPriority(), "Task priority should match.");
		assertEquals(42, task.getAssignedToUserId(), "Assigned user ID should match.");
		assertEquals("doubtfire", task.getAssignedToUsername(), "Assigned username should match.");
		assertFalse(task.isComplete(), "Task should initially be incomplete.");
		assertEquals(50, task.getProgress(), "Task progress should match.");
	}

	// TEST FOR TOSTRING METHOD
	@Test
	void testToStringContainsAllFields() {
		Task task = new Task(1, "Test Task", "This is a test task.", "High", 42, "doubtfire", false, 50);
		String taskString = task.toString();

		assertTrue(taskString.contains("1"), "toString should contain task ID.");
		assertTrue(taskString.contains("Test Task"), "toString should contain task title.");
		assertTrue(taskString.contains("High"), "toString should contain task priority.");
		assertTrue(taskString.contains("50%"), "toString should contain task progress.");
		assertTrue(taskString.contains("doubtfire"), "toString should contain assigned username.");
	}

	// TEST FOR SETTERS
	@Test
	void testSetter() {
		Task task = new Task(1, "Test Task", "This is a test task.", "High", 42, "doubtfire", false, 50);

		task.setTitle("Updated Title");
		task.setDescription("Updated Description");
		task.setPriority("High");
		task.setAssignedToUserId(42);
		task.setAssignedToUsername("doubtfire");
		task.setComplete(false);
		task.setProgress(50);

		assertEquals("Updated Title", task.getTitle(), "Title should be updated.");
		assertEquals("Updated Description", task.getDescription(), "Description should be updated.");
		assertEquals("High", task.getPriority(), "Priority should be updated.");
		assertEquals(42, task.getAssignedToUserId(), "User ID should be updated.");
		assertEquals("doubtfire", task.getAssignedToUsername(), "Username should be updated.");
		assertFalse(task.isComplete(), "isComplete should be updated.");
		assertEquals(50, task.getProgress(), "Progress should be updated.");
	}

}
