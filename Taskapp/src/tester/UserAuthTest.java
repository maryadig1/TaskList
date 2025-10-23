package tester;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import taskapp.UserAuth;

class UserAuthTest {

	// CREATE USERS TABLE BEFORE EACH TEST
	@BeforeEach
	void setUp() {
		UserAuth.createUsersTable();
	}

	// TEST USER REGISTRATION
	@Test
	void testRegisterUser() {
		String username = "testuser";
		String password = "testpass";

		Optional<UserAuth> user = UserAuth.registerUser(username, password);

		assertTrue(user.isPresent(), "User should be reigistered successfully.");
		assertEquals(username, user.get().getUsername(), "Username should match.");
	}

	// TEST USER REGISTRATION WITH EMPTY USERNAME
	@Test
	void testRegistration_EmptyUsername() {
		String username = "";
		String password = "testpass";

		Optional<UserAuth> user = UserAuth.registerUser(username, password);

		assertFalse(user.isPresent(), "Registration should fail for empty username.");
	}

	// TEST USER REGISTRATION WITH EMPTY PASSWORD
	@Test
	void testRegistration_EmptyPassword() {
		String username = "testuser";
		String password = "";

		Optional<UserAuth> user = UserAuth.registerUser(username, password);

		assertFalse(user.isPresent(), "Registration should fail for empty password.");
	}

	// TEST FOR USER REGISTRATION WITH EXISTING USERNAME
	@Test
	void testRegistration_ExistingUsername() {
		String username = "testuser";
		String password = "testpass";

		// FIRST REGISTRATION
		UserAuth.registerUser(username, password);

		// SECOND REGISTRATION W/ SAME USERNAME
		Optional<UserAuth> user = UserAuth.registerUser(username, password);

		assertFalse(user.isPresent(), "Registration should fail for duplicate usernames");
	}

	// TEST FOR VALID USERNAME
	@Test
	void testLoginUser_ValidUsername() {
		String username = "testuser";
		String password = "testpass";

		// FIRST REGISTRATION
		UserAuth.registerUser(username, password);

		// LOGIN W/ VALID CREDENTIALS
		Optional<UserAuth> user = UserAuth.loginUser(username, password);

		assertTrue(user.isPresent(), "Login should be successful with valid credentials.");
		assertEquals(username, user.get().getUsername(), "Username should match");
	}

	// TEST FOR INVALID PASSWORD
	@Test
	void testLoginUser_InvalidPassword() {
		String username = "testuser";
		String password = "testpass";
		String wrongPass = "wrongpass";

		// FIRST REGISTRATION
		UserAuth.registerUser(username, password);

		// LOGIN W/ INCORRECT PASSWORD
		Optional<UserAuth> user = UserAuth.loginUser(username, wrongPass);

		assertFalse(user.isPresent(), "Login should fail with invalid password.");
	}

	// TEST FOR NON-EXISTENT USER
	@Test
	void testLoginUser_NonExistentUser() {
		String username = "userDNE";
		String password = "testpass";

		Optional<UserAuth> user = UserAuth.loginUser(username, password);

		assertFalse(user.isPresent(), "Login should fail for non-existent user.");
	}

}
