package millimeeter.server.controller;

import static millimeeter.server.controller.response.MessageControllerResponses.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import millimeeter.server.dto.SentMessageDto;
import millimeeter.server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Validated
@Tag(name = "Message")
@RequestMapping("/api/v1/")
public class MessageController {

  @Autowired private MessageService messageService;

  @Operation(
      summary = "Get the conversation by match id",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(
                  name = "Got conversation with messages",
                  value = FIND_MESSAGES_BY_MATCH_ID_OK_WITH_MESSAGES_RESPONSE),
              @ExampleObject(
                  name = "Got conversation without messages",
                  value = FIND_MESSAGES_BY_MATCH_ID_OK_NO_MESSAGES_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Messages not found, match not exists",
                  value = UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE),
              @ExampleObject(
                  name = "Messages not found, profile not belongs to match",
                  value = UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value =
                      FIND_MESSAGES_BY_MATCH_ID_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid find conversation messages request",
                  value = FIND_MESSAGES_BY_MATCH_ID_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @GetMapping("/conversations/{id}")
  public ResponseEntity<Object> findMessagesByMatchId(
      @PathVariable @Positive(message = "The match id must be a positive number") Long id) {
    return messageService.findMessagesByMatchId(id);
  }

  @Operation(
      summary = "Send the message",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "201",
      description = "CREATED",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {@ExampleObject(name = "Message sent", value = SEND_CREATED_RESPONSE)})
      })
  @ApiResponse(
      responseCode = "400",
      description = "BAD REQUEST",
      content = {
        @Content(
            mediaType = "application/json",
            examples = {@ExampleObject(name = "Bad request", value = SEND_BAD_REQUEST_RESPONSE)})
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Message not sent, match not exists",
                  value = UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE),
              @ExampleObject(
                  name = "Message not sent, parent message not exists",
                  value = SEND_UNPROCESSABLE_ENTITY_PARENT_MESSAGE_NOT_EXISTS_RESPONSE),
              @ExampleObject(
                  name = "Message not sent, profile not belongs to match",
                  value = UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value = SEND_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid send request",
                  value = SEND_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @PostMapping("/messages")
  public ResponseEntity<Object> send(@Valid @RequestBody SentMessageDto sentMessageDto) {
    return messageService.send(sentMessageDto);
  }

  @Operation(
      summary = "React to the message",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(name = "Reacted to message", value = REACT_TO_MESSAGE_OK_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Not reacted, message not exists",
                  value = UNPROCESSABLE_ENTITY_MESSAGE_NOT_EXISTS_RESPONSE),
              @ExampleObject(
                  name = "Not reacted, profile not belongs to match",
                  value = UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value = REACT_TO_MESSAGE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid react to message request",
                  value = REACT_TO_MESSAGE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @PutMapping("/messages/{id}/{reaction}")
  public ResponseEntity<Object> reactToMessage(
      @PathVariable @NotNull @Positive(message = "The message id must be a positive number")
          Long id,
      @PathVariable
          @Pattern(
              regexp = "LIKE|SUPER|CARE|HAHA|WOW|CRY|WRR",
              message = "Reaction must have value LIKE, SUPER, CARE, HAHA, WOW, CRY or WRR")
          String reaction) {
    return messageService.reactToMessageWithId(id, reaction);
  }

  @Operation(
      summary = "Delete reaction from the message",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(
                  name = "Deleted reaction",
                  value = DELETE_REACTION_FROM_MESSAGE_OK_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Reaction not deleted, message not exists",
                  value = UNPROCESSABLE_ENTITY_MESSAGE_NOT_EXISTS_RESPONSE),
              @ExampleObject(
                  name = "Reaction not deleted, profile not belongs to match",
                  value = UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value =
                      DELETE_REACTION_FROM_MESSAGE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid delete reaction request",
                  value = DELETE_REACTION_FROM_MESSAGE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @PutMapping("/messages/{id}")
  public ResponseEntity<Object> deleteReactionFromMessage(
      @PathVariable @NotNull @Positive(message = "The message id must be a positive number")
          Long id) {
    return messageService.deleteReactionFromMessageWithId(id);
  }

  @Operation(
      summary = "Read messages in the conversation",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Messages in conversation read",
                  value = READ_MESSAGES_IN_CONVERSATION_AND_EXCEPT_OK_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Not read, match not exists",
                  value = UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE),
              @ExampleObject(
                  name = "Not read, profile not belongs to match",
                  value = UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value =
                      READ_MESSAGES_IN_CONVERSATION_AND_EXCEPT_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid read messages in conversation request",
                  value =
                      READ_MESSAGES_IN_CONVERSATION_AND_EXCEPT_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @PutMapping("/messages/read/{matchId}")
  public ResponseEntity<Void> readMessagesInConversation(
      @PathVariable @NotNull @Positive(message = "The match id must be a positive number")
          Long matchId) {
    return messageService.readMessagesInConversationWithMatchId(matchId);
  }

  @Operation(
      summary = "Set the messages as delivered",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Status set as delivered",
                  value = SET_MESSAGES_STATUS_AS_DELIVERED_OK_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Profile not exists",
                  value =
                      SET_MESSAGES_STATUS_AS_DELIVERED_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            })
      })
  @PutMapping("/messages/setAsDelivered")
  public ResponseEntity<Void> setMessagesAsDelivered() {
    return messageService.setMessagesStatusAsDelivered();
  }
}
