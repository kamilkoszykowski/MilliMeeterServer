package millimeeter.server.service;

import java.util.List;
import java.util.Objects;
import millimeeter.server.dto.MessageDto;
import millimeeter.server.dto.SentMessageDto;
import millimeeter.server.enums.MessageReaction;
import millimeeter.server.model.Message;
import millimeeter.server.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MessageService {

  private final MessageRepository messageRepository;
  private final AuthUtils authUtils;

  @Autowired
  public MessageService(MessageRepository messageRepository, AuthUtils authUtils) {
    this.messageRepository = messageRepository;
    this.authUtils = authUtils;
  }

  public List<MessageDto> findMessagesByMatchId(Long matchId) {
    authUtils.checkIfProfileExists();
    authUtils.checkIfMatchExists(matchId);
    authUtils.checkIfProfileBelongsToMatch(matchId);
    return messageRepository.findMessagesByMatchId(matchId);
  }

  public Message send(SentMessageDto sentMessageDto) {
    Long parentMessageId = sentMessageDto.getParentMessageId();
    if (parentMessageId != null) {
      if (messageRepository.findById(parentMessageId).isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY, "Parent message not exists");
      }
    }
    authUtils.checkIfProfileExists();
    long matchId = sentMessageDto.getMatchId();
    authUtils.checkIfMatchExists(matchId);
    authUtils.checkIfProfileBelongsToMatch(matchId);
    return messageRepository.save(new Message(authUtils.getProfileId(), sentMessageDto));
  }

  public Message reactToMessageWithId(Long messageId, String messageReaction) {
    authUtils.checkIfProfileExists();
    checkIfMessageExists(messageId);
    Message message = messageRepository.getReferenceById(messageId);
    authUtils.checkIfProfileBelongsToMatchContainingMessage(message.getMatchId());
    if (Objects.equals(message.getSenderId(), authUtils.getProfileId())) {
      message.setSenderReaction(MessageReaction.valueOf(messageReaction));
    } else {
      message.setReceiverReaction(MessageReaction.valueOf(messageReaction));
    }
    return messageRepository.save(message);
  }

  public Message deleteReactionFromMessageWithId(Long messageId) {
    authUtils.checkIfProfileExists();
    checkIfMessageExists(messageId);
    Message message = messageRepository.getReferenceById(messageId);
    authUtils.checkIfProfileBelongsToMatchContainingMessage(message.getMatchId());
    if (Objects.equals(message.getSenderId(), authUtils.getProfileId())) {
      message.setSenderReaction(null);
    } else {
      message.setReceiverReaction(null);
    }
    return messageRepository.save(message);
  }

  public void readMessagesInConversationWithMatchId(Long matchId) {
    authUtils.checkIfProfileExists();
    authUtils.checkIfMatchExists(matchId);
    authUtils.checkIfProfileBelongsToMatch(matchId);
    messageRepository.readMessagesInConversationByProfileIdAndMatchId(
        authUtils.getProfileId(), matchId);
  }

  public void setMessagesStatusAsDelivered() {
    authUtils.checkIfProfileExists();
    messageRepository.setMessagesStatusAsDeliveredByProfileId(authUtils.getProfileId());
  }

  public void checkIfMessageExists(Long messageId) {
    if (messageRepository.findById(messageId).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Message not exists");
    }
  }
}
