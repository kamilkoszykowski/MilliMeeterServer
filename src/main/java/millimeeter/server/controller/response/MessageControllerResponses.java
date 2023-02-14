package millimeeter.server.controller.response;

public class MessageControllerResponses {

  // SHARED
  public static final String UNPROCESSABLE_ENTITY_MESSAGE_NOT_EXISTS_RESPONSE =
      "The message with given id does not exist";
  public static final String
      UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_CONTAINING_GIVEN_MESSAGE_RESPONSE =
          "The profile does not belong to match containing the given message";
  public static final String UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE =
      "The match with given id does not exist";
  public static final String UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE =
      "The profile does not belong to the given match";

  // ENDPOINT: "/messages/{id}"
  public static final String DELETE_REACTION_FROM_MESSAGE_OK_RESPONSE =
      "{\"id\":233,\"senderId\":53,\"matchId\":518,\"content\":\"content\",\"parentMessageId\":null,\"senderReaction\":null,\"receiverReaction\":null,\"status\":\"SENT\",\"sentAt\":\"2023-02-10T10:53:13.667864\",\"seenAt\":null,\"_links\":{\"reply\":{\"href\":\"http://localhost/api/v1/messages\"},\"react"
          + " like\":{\"href\":\"http://localhost/api/v1/messages/233/LIKE\"},\"react"
          + " super\":{\"href\":\"http://localhost/api/v1/messages/233/SUPER\"},\"react"
          + " haha\":{\"href\":\"http://localhost/api/v1/messages/233/HAHA\"},\"react"
          + " cry\":{\"href\":\"http://localhost/api/v1/messages/233/CRY\"},\"react"
          + " wrr\":{\"href\":\"http://localhost/api/v1/messages/233/WRR\"},\"react"
          + " care\":{\"href\":\"http://localhost/api/v1/messages/233/CARE\"},\"delete"
          + " reaction\":{\"href\":\"http://localhost/api/v1/messages/233\"}}}";
  public static final String DELETE_REACTION_FROM_MESSAGE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"The message id must be a positive number\"]}";

  public static final String
      DELETE_REACTION_FROM_MESSAGE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
          "Cannot delete reaction from the message due to non-existing profile";

  // ENDPOINT: "/conversations/{id}"
  public static final String FIND_MESSAGES_BY_MATCH_ID_OK_NO_MESSAGES_RESPONSE = "{}";
  public static final String FIND_MESSAGES_BY_MATCH_ID_OK_WITH_MESSAGES_RESPONSE =
      "{\"_embedded\":{\"messageDtoList\":[{\"id\":225,\"senderId\":53,\"content\":\"content\",\"parentMessageId\":null,\"senderReaction\":null,\"receiverReaction\":null,\"status\":\"SENT\",\"sentAt\":\"2023-02-10T10:53:13.240474\",\"seenAt\":null,\"_links\":{\"reply\":{\"href\":\"http://localhost/api/v1/messages\"},\"react"
          + " like\":{\"href\":\"http://localhost/api/v1/messages/225/LIKE\"},\"react"
          + " super\":{\"href\":\"http://localhost/api/v1/messages/225/SUPER\"},\"react"
          + " haha\":{\"href\":\"http://localhost/api/v1/messages/225/HAHA\"},\"react"
          + " cry\":{\"href\":\"http://localhost/api/v1/messages/225/CRY\"},\"react"
          + " wrr\":{\"href\":\"http://localhost/api/v1/messages/225/WRR\"},\"react"
          + " care\":{\"href\":\"http://localhost/api/v1/messages/225/CARE\"},\"delete"
          + " reaction\":{\"href\":\"http://localhost/api/v1/messages/225\"}}}]}}";
  public static final String FIND_MESSAGES_BY_MATCH_ID_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"The match id must be a positive number\"]}";
  public static final String
      FIND_MESSAGES_BY_MATCH_ID_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
          "Cannot get the conversation due to non-existing profile";

  // ENDPOINT: "/conversations/{id}"
  public static final String REACT_TO_MESSAGE_OK_RESPONSE =
      "{\"id\":223,\"senderId\":53,\"matchId\":501,\"content\":\"content\",\"parentMessageId\":null,\"senderReaction\":\"LIKE\",\"receiverReaction\":null,\"status\":\"SENT\",\"sentAt\":\"2023-02-10T10:53:12.971228\",\"seenAt\":null,\"_links\":{\"reply\":{\"href\":\"http://localhost/api/v1/messages\"},\"react"
          + " like\":{\"href\":\"http://localhost/api/v1/messages/223/LIKE\"},\"react"
          + " super\":{\"href\":\"http://localhost/api/v1/messages/223/SUPER\"},\"react"
          + " haha\":{\"href\":\"http://localhost/api/v1/messages/223/HAHA\"},\"react"
          + " cry\":{\"href\":\"http://localhost/api/v1/messages/223/CRY\"},\"react"
          + " wrr\":{\"href\":\"http://localhost/api/v1/messages/223/WRR\"},\"react"
          + " care\":{\"href\":\"http://localhost/api/v1/messages/223/CARE\"},\"delete"
          + " reaction\":{\"href\":\"http://localhost/api/v1/messages/223\"}}}";
  public static final String REACT_TO_MESSAGE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"The message id must be a positive number\",\"Reaction must have value LIKE,"
          + " SUPER, CARE, HAHA, WOW, CRY or WRR\"]}";
  public static final String REACT_TO_MESSAGE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot react to the message due to non-existing profile";

  // ENDPOINT: "/conversations/{id}"
  public static final String READ_MESSAGES_IN_CONVERSATION_AND_EXCEPT_OK_RESPONSE = "";
  public static final String
      READ_MESSAGES_IN_CONVERSATION_AND_EXCEPT_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
          "{\"errors\":[\"The match id must be a positive number\"]}";
  public static final String
      READ_MESSAGES_IN_CONVERSATION_AND_EXCEPT_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
          "Cannot read the messages due to non-existing profile";

  // ENDPOINT: "/messages"
  public static final String SEND_BAD_REQUEST_RESPONSE =
      "{\"errors\":[\"The content is required\"]}";
  public static final String SEND_CREATED_RESPONSE =
      "{\"id\":221,\"senderId\":53,\"matchId\":498,\"content\":\"content\",\"parentMessageId\":null,\"senderReaction\":null,\"receiverReaction\":null,\"status\":\"SENT\",\"sentAt\":\"2023-02-10T10:53:12.7179983\",\"seenAt\":null,\"_links\":{\"reply\":{\"href\":\"http://localhost/api/v1/messages\"},\"react"
          + " like\":{\"href\":\"http://localhost/api/v1/messages/221/LIKE\"},\"react"
          + " super\":{\"href\":\"http://localhost/api/v1/messages/221/SUPER\"},\"react"
          + " haha\":{\"href\":\"http://localhost/api/v1/messages/221/HAHA\"},\"react"
          + " cry\":{\"href\":\"http://localhost/api/v1/messages/221/CRY\"},\"react"
          + " wrr\":{\"href\":\"http://localhost/api/v1/messages/221/WRR\"},\"react"
          + " care\":{\"href\":\"http://localhost/api/v1/messages/221/CARE\"},\"delete"
          + " reaction\":{\"href\":\"http://localhost/api/v1/messages/221\"}}}";
  public static final String SEND_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"The parent message id must be a positive number\"]}";
  public static final String SEND_UNPROCESSABLE_ENTITY_PARENT_MESSAGE_NOT_EXISTS_RESPONSE =
      "The parent message with given id does not exist";
  public static final String SEND_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot send the message due to non-existing profile";

  // ENDPOINT: "/conversations/{id}"
  public static final String SET_MESSAGES_STATUS_AS_DELIVERED_OK_RESPONSE = "";
  public static final String
      SET_MESSAGES_STATUS_AS_DELIVERED_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
          "Cannot set the messages as delivered due to non-existing profile";
}
