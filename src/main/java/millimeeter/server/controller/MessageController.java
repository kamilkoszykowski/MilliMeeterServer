package millimeeter.server.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import millimeeter.server.dto.MessageDto;
import millimeeter.server.dto.SentMessageDto;
import millimeeter.server.model.Message;
import millimeeter.server.service.MessageService;
import millimeeter.server.service.assembler.MessageDtoModelAssembler;
import millimeeter.server.service.assembler.MessageModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Validated
@RequestMapping("/api/v1/")
public class MessageController {

  private final MessageService messageService;
  private final MessageModelAssembler messageModelAssembler;
  private final MessageDtoModelAssembler messageDtoModelAssembler;

  @Autowired
  public MessageController(
      MessageService messageService,
      MessageModelAssembler messageModelAssembler,
      MessageDtoModelAssembler messageDtoModelAssembler) {
    this.messageService = messageService;
    this.messageModelAssembler = messageModelAssembler;
    this.messageDtoModelAssembler = messageDtoModelAssembler;
  }

  @GetMapping("/conversations/{id}")
  public ResponseEntity<CollectionModel<EntityModel<MessageDto>>> findMessagesByMatchId(
      @PathVariable @Positive(message = "The match id must be a positive number") Long id) {
    List<EntityModel<MessageDto>> messagesModel =
        messageService.findMessagesByMatchId(id).stream()
            .map(messageDtoModelAssembler::toModel)
            .collect(Collectors.toList());
    return new ResponseEntity<>(CollectionModel.of(messagesModel), HttpStatus.OK);
  }

  @PostMapping("/messages")
  public ResponseEntity<Object> send(@Valid @RequestBody SentMessageDto sentMessageDto) {
    return new ResponseEntity<>(
        messageModelAssembler.toModel(messageService.send(sentMessageDto)), HttpStatus.CREATED);
  }

  @PutMapping("/messages/{id}/{reaction}")
  public ResponseEntity<EntityModel<Message>> reactToMessage(
      @PathVariable @NotNull @Positive(message = "The message id must be a positive number")
          Long id,
      @PathVariable
          @Pattern(
              regexp = "LIKE|SUPER|CARE|HAHA|WOW|CRY|WRR",
              message = "Reaction must have value LIKE, SUPER, CARE, HAHA, WOW, CRY or WRR")
          String reaction) {
    return new ResponseEntity<>(
        messageModelAssembler.toModel(messageService.reactToMessageWithId(id, reaction)),
        HttpStatus.OK);
  }

  @PutMapping("/messages/{id}")
  public ResponseEntity<Object> deleteReactionFromMessage(
      @PathVariable @NotNull @Positive(message = "The message id must be a positive number")
          Long id) {
    return new ResponseEntity<>(
        messageModelAssembler.toModel(messageService.deleteReactionFromMessageWithId(id)),
        HttpStatus.OK);
  }

  @PutMapping("/messages/read/{matchId}")
  public ResponseEntity<Void> readMessagesInConversation(
      @PathVariable @NotNull @Positive(message = "The match id must be a positive number")
          Long matchId) {
    messageService.readMessagesInConversationWithMatchId(matchId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PutMapping("/messages/setAsDelivered")
  public ResponseEntity<Void> setMessagesAsDelivered() {
    messageService.setMessagesStatusAsDelivered();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
