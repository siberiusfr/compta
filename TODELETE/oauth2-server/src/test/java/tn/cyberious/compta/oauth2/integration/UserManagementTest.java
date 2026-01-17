package tn.cyberious.compta.oauth2.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.cyberious.compta.oauth2.BaseIntegrationTest;

/** Integration tests for User Management API. */
@DisplayName("User Management Tests")
class UserManagementTest extends BaseIntegrationTest {

  private String adminToken;

  @BeforeEach
  void obtainAdminToken() throws Exception {
    // Get an admin token for API calls
    MvcResult result =
        mockMvc
            .perform(
                post("/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", basicAuth(GATEWAY_CLIENT_ID, GATEWAY_CLIENT_SECRET))
                    .param("grant_type", "client_credentials")
                    .param("scope", "openid read write"))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    adminToken = JsonPath.read(responseBody, "$.access_token");
  }

  @Nested
  @DisplayName("List Users")
  class ListUsersTests {

    @Test
    @DisplayName("Should list all users with valid token")
    void shouldListAllUsersWithValidToken() throws Exception {
      mockMvc
          .perform(get("/api/users").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[*].username", hasItem("admin")))
          .andExpect(jsonPath("$[*].username", hasItem("user")));
    }

    @Test
    @DisplayName("Should reject unauthenticated request")
    void shouldRejectUnauthenticatedRequest() throws Exception {
      mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() throws Exception {
      mockMvc
          .perform(get("/api/users").header("Authorization", "Bearer invalid-token"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Create User")
  class CreateUserTests {

    @Test
    @DisplayName("Should create new user")
    void shouldCreateNewUser() throws Exception {
      String uniqueUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
      String requestBody =
          """
          {
            "username": "%s",
            "email": "%s@test.com",
            "password": "TestPassword123!",
            "firstName": "Test",
            "lastName": "User",
            "roles": ["ROLE_USER"]
          }
          """
              .formatted(uniqueUsername, uniqueUsername);

      mockMvc
          .perform(
              post("/api/users")
                  .header("Authorization", "Bearer " + adminToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username").value(uniqueUsername))
          .andExpect(jsonPath("$.email").value(uniqueUsername + "@test.com"))
          .andExpect(jsonPath("$.firstName").value("Test"))
          .andExpect(jsonPath("$.lastName").value("User"))
          .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Should reject duplicate username")
    void shouldRejectDuplicateUsername() throws Exception {
      String requestBody =
          """
          {
            "username": "admin",
            "email": "another@test.com",
            "password": "TestPassword123!",
            "firstName": "Another",
            "lastName": "Admin"
          }
          """;

      mockMvc
          .perform(
              post("/api/users")
                  .header("Authorization", "Bearer " + adminToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject invalid email")
    void shouldRejectInvalidEmail() throws Exception {
      String requestBody =
          """
          {
            "username": "newuser",
            "email": "invalid-email",
            "password": "TestPassword123!",
            "firstName": "New",
            "lastName": "User"
          }
          """;

      mockMvc
          .perform(
              post("/api/users")
                  .header("Authorization", "Bearer " + adminToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject missing required fields")
    void shouldRejectMissingRequiredFields() throws Exception {
      String requestBody =
          """
          {
            "username": "newuser"
          }
          """;

      mockMvc
          .perform(
              post("/api/users")
                  .header("Authorization", "Bearer " + adminToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Get User by ID")
  class GetUserByIdTests {

    @Test
    @DisplayName("Should return user by ID")
    void shouldReturnUserById() throws Exception {
      // First get the list to find admin user ID
      MvcResult listResult =
          mockMvc
              .perform(get("/api/users").header("Authorization", "Bearer " + adminToken))
              .andExpect(status().isOk())
              .andReturn();

      String listBody = listResult.getResponse().getContentAsString();
      List<String> ids = JsonPath.read(listBody, "$[?(@.username=='admin')].id");
      String adminId = ids.get(0);

      mockMvc
          .perform(get("/api/users/" + adminId).header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username").value("admin"))
          .andExpect(jsonPath("$.id").value(adminId));
    }

    @Test
    @DisplayName("Should return 404 for non-existent user")
    void shouldReturn404ForNonExistentUser() throws Exception {
      String randomId = UUID.randomUUID().toString();

      mockMvc
          .perform(get("/api/users/" + randomId).header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 for invalid UUID")
    void shouldReturn400ForInvalidUuid() throws Exception {
      mockMvc
          .perform(get("/api/users/not-a-uuid").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Update User")
  class UpdateUserTests {

    @Test
    @DisplayName("Should update user details")
    void shouldUpdateUserDetails() throws Exception {
      // First create a user to update
      String uniqueUsername = "updatetest_" + UUID.randomUUID().toString().substring(0, 8);
      String createBody =
          """
          {
            "username": "%s",
            "email": "%s@test.com",
            "password": "TestPassword123!",
            "firstName": "Original",
            "lastName": "Name"
          }
          """
              .formatted(uniqueUsername, uniqueUsername);

      MvcResult createResult =
          mockMvc
              .perform(
                  post("/api/users")
                      .header("Authorization", "Bearer " + adminToken)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(createBody))
              .andExpect(status().isOk())
              .andReturn();

      String userId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

      // Now update the user
      String updateBody =
          """
          {
            "firstName": "Updated",
            "lastName": "Name"
          }
          """;

      mockMvc
          .perform(
              put("/api/users/" + userId)
                  .header("Authorization", "Bearer " + adminToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(updateBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.firstName").value("Updated"))
          .andExpect(jsonPath("$.lastName").value("Name"));
    }
  }

  @Nested
  @DisplayName("Delete User")
  class DeleteUserTests {

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() throws Exception {
      // First create a user to delete
      String uniqueUsername = "deletetest_" + UUID.randomUUID().toString().substring(0, 8);
      String createBody =
          """
          {
            "username": "%s",
            "email": "%s@test.com",
            "password": "TestPassword123!",
            "firstName": "Delete",
            "lastName": "Me"
          }
          """
              .formatted(uniqueUsername, uniqueUsername);

      MvcResult createResult =
          mockMvc
              .perform(
                  post("/api/users")
                      .header("Authorization", "Bearer " + adminToken)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(createBody))
              .andExpect(status().isOk())
              .andReturn();

      String userId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

      // Delete the user
      mockMvc
          .perform(delete("/api/users/" + userId).header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isNoContent());

      // Verify user is deleted
      mockMvc
          .perform(get("/api/users/" + userId).header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("User Roles Management")
  class UserRolesTests {

    @Test
    @DisplayName("Should get user roles")
    void shouldGetUserRoles() throws Exception {
      // Get admin user ID
      MvcResult listResult =
          mockMvc
              .perform(get("/api/users").header("Authorization", "Bearer " + adminToken))
              .andExpect(status().isOk())
              .andReturn();

      String listBody = listResult.getResponse().getContentAsString();
      List<String> ids = JsonPath.read(listBody, "$[?(@.username=='admin')].id");
      String adminId = ids.get(0);

      mockMvc
          .perform(
              get("/api/users/" + adminId + "/roles")
                  .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[*].name", hasItem("ROLE_ADMIN")));
    }
  }

  @Nested
  @DisplayName("Enable/Disable User")
  class EnableDisableUserTests {

    @Test
    @DisplayName("Should disable and enable user")
    void shouldDisableAndEnableUser() throws Exception {
      // Create a user to disable
      String uniqueUsername = "disabletest_" + UUID.randomUUID().toString().substring(0, 8);
      String createBody =
          """
          {
            "username": "%s",
            "email": "%s@test.com",
            "password": "TestPassword123!",
            "firstName": "Disable",
            "lastName": "Test"
          }
          """
              .formatted(uniqueUsername, uniqueUsername);

      MvcResult createResult =
          mockMvc
              .perform(
                  post("/api/users")
                      .header("Authorization", "Bearer " + adminToken)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(createBody))
              .andExpect(status().isOk())
              .andReturn();

      String userId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

      // Disable the user
      mockMvc
          .perform(
              patch("/api/users/" + userId + "/disable")
                  .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.enabled").value(false));

      // Enable the user
      mockMvc
          .perform(
              patch("/api/users/" + userId + "/enable")
                  .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.enabled").value(true));
    }
  }
}
