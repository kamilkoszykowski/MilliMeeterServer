package millimeeter.server.controller.response;

public class SwipeControllerResponses {

  // ENDPOINT: "/swipes/{id}/{direction}"
  public static final String SWIPE_CONFLICT_ALREADY_SWIPED_RESPONSE =
      "You already swiped that profile";
  public static final String SWIPE_LEFT_CREATED_SWIPE_RESPONSE =
      "{\"id\":380,\"senderId\":53,\"receiverId\":42,\"direction\":\"LEFT\",\"swipedAt\":\"2023-02-10T12:34:23.3346799\",\"_links\":{\"swipes"
          + " left amount\":{\"href\":\"http://localhost/api/v1/profiles/swipesLeft\"}}}";
  public static final String SWIPE_NOT_FOUND_SWIPED_PROFILE_NOT_EXISTS_YET_RESPONSE =
      "The profile you swiped does not exist yet";
  public static final String SWIPE_UNPROCESSABLE_ENTITY_CANNOT_SWIPE_YOURSELF_RESPONSE =
      "You cannot swipe yourself";
  public static final String SWIPE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"Profile id must be a positive number\",\"The swipe value must be LEFT or"
          + " RIGHT\"]}";
  public static final String SWIPE_UNPROCESSABLE_ENTITY_NO_SWIPES_LEFT_RESPONSE =
      "You have no swipes left";
  public static final String SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot swipe the profile due to non-existing profile";
  public static final String SWIPE_RIGHT_CREATED_MATCH_RESPONSE =
      "{\"id\":522,\"profileId1\":53,\"profileId2\":42,\"matchedAt\":\"2023-02-10T12:34:23.0354061\",\"_links\":{\"conversation\":{\"href\":\"http://localhost/api/v1/conversations/522\"},\"send"
          + " message\":{\"href\":\"http://localhost/api/v1/messages\"},\"delete\":{\"href\":\"http://localhost/api/v1/matches/522\"},\"swipes"
          + " left amount\":{\"href\":\"http://localhost/api/v1/profiles/swipesLeft\"}}}";
  public static final String SWIPE_RIGHT_CREATED_SWIPE_RESPONSE =
      "{\"id\":379,\"senderId\":53,\"receiverId\":42,\"direction\":\"RIGHT\",\"swipedAt\":\"2023-02-10T12:34:23.235589\",\"_links\":{\"swipes"
          + " left amount\":{\"href\":\"http://localhost/api/v1/profiles/swipesLeft\"}}}";
}
