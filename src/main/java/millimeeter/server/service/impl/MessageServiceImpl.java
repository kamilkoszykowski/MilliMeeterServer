package millimeeter.server.service.impl;

import static millimeeter.server.controller.response.MessageControllerResponses.*;

import java.util.List;
import java.util.stream.Collectors;
import millimeeter.server.dto.MessageDto;
import millimeeter.server.dto.SentMessageDto;
import millimeeter.server.enums.MessageReaction;
import millimeeter.server.model.Message;
import millimeeter.server.repository.MatchRepository;
import millimeeter.server.repository.MessageRepository;
import millimeeter.server.service.AuthUtils;
import millimeeter.server.service.MessageService;
import millimeeter.server.service.assembler.MessageDtoModelAssembler;
import millimeeter.server.service.assembler.MessageModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

  @Autowired private MessageRepository messageRepository;
  @Autowired private AuthUtils authUtils;
  @Autowired private MessageModelAssembler messageModelAssembler;
  @Autowired private MessageDtoModelAssembler messageDtoModelAssembler;
  @Autowired private MatchRepository matchRepository;

  @Override
  public ResponseEntity<Object> findMessagesByMatchId(Long matchId) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      if (authUtils.matchExistsById(matchId)) {
        if (!authUtils.profileBelongsToMatch(matchId)) {
          return new ResponseEntity<>(
              UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE,
              HttpStatus.UNPROCESSABLE_ENTITY);
        }
        List<MessageDto> messages = messageRepository.findMessagesByMatchId(matchId);
        List<EntityModel<MessageDto>> messagesModel =
            messages.stream().map(messageDtoModelAssembler::toModel).collect(Collectors.toList());
        return new ResponseEntity<>(CollectionModel.of(messagesModel), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(
            UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE, HttpStatus.UNPROCESSABLE_ENTITY);
      }
    } else {
      return new ResponseEntity<>(
          FIND_MESSAGES_BY_MATCH_ID_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> send(SentMessageDto sentMessageDto) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      if (!authUtils.matchExistsById(sentMessageDto.getMatchId())) {
        return new ResponseEntity<>(
            UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE, HttpStatus.UNPROCESSABLE_ENTITY);
      }
      if (sentMessageDto.getParentMessageId() != null) {
        if (messageRepository.findById(sentMessageDto.getParentMessageId()).isEmpty()) {
          return new ResponseEntity<>(
              SEND_UNPROCESSABLE_ENTITY_PARENT_MESSAGE_NOT_EXISTS_RESPONSE,
              HttpStatus.UNPROCESSABLE_ENTITY);
        }
      }
      if (authUtils.profileBelongsToMatch(sentMessageDto.getMatchId())) {
        Message message = new Message(authUtils.getProfileId(), sentMessageDto);
        return new ResponseEntity<>(
            messageModelAssembler.toModel(messageRepository.save(message)), HttpStatus.CREATED);
      } else {
        return new ResponseEntity<>(
            UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE,
            HttpStatus.UNPROCESSABLE_ENTITY);
      }
    } else {
      return new ResponseEntity<>(
          SEND_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE, HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> reactToMessageWithId(Long messageId, String messageReaction) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      if (messageRepository.findById(messageId).isEmpty()) {
        return new ResponseEntity<>(
            UNPROCESSABLE_ENTITY_MESSAGE_NOT_EXISTS_RESPONSE, HttpStatus.UNPROCESSABLE_ENTITY);
      }
      Message message = messageRepository.getReferenceById(messageId);
      if (authUtils.profileBelongsToMatch(message.getMatchId())) {
        if (message.getSenderId() == authUtils.getProfileId()) {
          message.setSenderReaction(MessageReaction.valueOf(messageReaction));
        } else {
          message.setReceiverReaction(MessageReaction.valueOf(messageReaction));
        }
        return new ResponseEntity<>(
            messageModelAssembler.toModel(messageRepository.save(message)), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(
            UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_CONTAINING_GIVEN_MESSAGE_RESPONSE,
            HttpStatus.UNPROCESSABLE_ENTITY);
      }
    } else {
      return new ResponseEntity<>(
          REACT_TO_MESSAGE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> deleteReactionFromMessageWithId(Long messageId) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      if (messageRepository.findById(messageId).isEmpty()) {
        return new ResponseEntity<>(
            UNPROCESSABLE_ENTITY_MESSAGE_NOT_EXISTS_RESPONSE, HttpStatus.UNPROCESSABLE_ENTITY);
      }
      Message message = messageRepository.getReferenceById(messageId);
      if (authUtils.profileBelongsToMatch(message.getMatchId())) {
        if (message.getSenderId() == authUtils.getProfileId()) {
          message.setSenderReaction(null);
        } else {
          message.setReceiverReaction(null);
        }
        return new ResponseEntity<>(
            messageModelAssembler.toModel(messageRepository.save(message)), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(
            UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_CONTAINING_GIVEN_MESSAGE_RESPONSE,
            HttpStatus.UNPROCESSABLE_ENTITY);
      }
    } else {
      return new ResponseEntity<>(
          DELETE_REACTION_FROM_MESSAGE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Void> readMessagesInConversationWithMatchId(Long matchId) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      if (matchRepository.findById(matchId).isPresent()) {
        if (!authUtils.profileBelongsToMatch(matchId)) {
          throw new ResponseStatusException(
              HttpStatus.UNPROCESSABLE_ENTITY,
              UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE);
        }
        messageRepository.readMessagesInConversationByProfileIdAndMatchId(
            authUtils.getProfileId(), matchId);
      } else {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY, UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE);
      }
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          READ_MESSAGES_IN_CONVERSATION_AND_EXCEPT_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE);
    }
  }

  @Override
  public ResponseEntity<Void> setMessagesStatusAsDelivered() {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      messageRepository.setMessagesStatusAsDeliveredByProfileId(authUtils.getProfileId());
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          SET_MESSAGES_STATUS_AS_DELIVERED_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE);
    }
  }
}
