package millimeeter.server.service;

import millimeeter.server.dto.SentMessageDto;
import org.springframework.http.ResponseEntity;

public interface MessageService {
  ResponseEntity<Object> findMessagesByMatchId(Long matchId); // GET

  ResponseEntity<Object> send(SentMessageDto sentMessageDto); // POST

  ResponseEntity<Object> reactToMessageWithId(Long messageId, String messageReaction); // PUT

  ResponseEntity<Object> deleteReactionFromMessageWithId(Long messageId); // PUT

  ResponseEntity<Void> readMessagesInConversationWithMatchId(Long matchId); // PUT

  ResponseEntity<Void> setMessagesStatusAsDelivered(); // PUT
}
