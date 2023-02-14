package millimeeter.server.controller.response;

public class MatchControllerResponses {

  // ENDPOINT: "/matches"
  public static final String FIND_MATCHES_OK_NO_MATCHES_RESPONSE = "{}";
  public static final String FIND_MATCHES_OK_WITH_MATCHES_RESPONSE =
      "{\"_embedded\":{\"matchDtoList\":[{\"id\":438,\"profileId\":42,\"firstName\":\"ProfileTwo\",\"photos\":[\"anotherPhoto1.jpg\",\"anotherPhoto2.jpg\"],\"matchedAt\":\"2023-02-09T22:45:36.620419\",\"_links\":{\"conversation\":{\"href\":\"http://localhost/api/v1/conversations/438\"},\"delete\":{\"href\":\"http://localhost/api/v1/matches/438\"}}}]}}";
  public static final String FIND_MATCHES_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot get matches due to non-existing profile";

  // ENDPOINT: "/conversations"
  public static final String FIND_MATCHES_WITH_MESSAGES_OK_NO_MATCHES_RESPONSE = "{}";
  public static final String FIND_MATCHES_WITH_MESSAGES_OK_WITH_MATCHES_RESPONSE =
      "{\"_embedded\":{\"matchWithMessagesDtoList\":[{\"id\":471,\"profileId\":42,\"firstName\":\"ProfileTwo\",\"photos\":[\"anotherPhoto1.jpg\",\"anotherPhoto2.jpg\"],\"senderId\":53,\"lastMessageContent\":\"content\",\"lastMessageStatus\":\"SENT\",\"lastMessageSentAt\":\"2023-02-10T10:49:51.88033\",\"_links\":{\"conversation\":{\"href\":\"http://localhost/api/v1/conversations/471\"},\"delete\":{\"href\":\"http://localhost/api/v1/matches/471\"}}}]}}";
  public static final String
      FIND_MATCHES_WITH_MESSAGES_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
          "Cannot get matches with messages due to non-existing profile";

  // ENDPOINT: "/matches/{id}"
  public static final String DELETE_MATCH_NO_CONTENT_RESPONSE = "";
  public static final String DELETE_MATCH_UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE =
      "The match with given id does not exist";
  public static final String DELETE_MATCH_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"The match id must be a positive number\"]}";
  public static final String DELETE_MATCH_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot delete the match due to non-existing profile";
}
