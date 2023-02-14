package millimeeter.server.controller.response;

public class UserControllerResponses {

  // ENDPOINT: "/users"
  public static final String CREATE_USER_CONFLICT_RESPONSE = "User already exists";
  public static final String CREATE_USER_CREATED_RESPONSE =
      "{\"id\":\"mockUser\",\"profileId\":55,\"_links\":{\"create"
          + " profile\":{\"href\":\"http://localhost/api/v1/profiles/\"}}}";
}
