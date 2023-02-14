package millimeeter.server.repository;

import java.util.List;
import millimeeter.server.dto.MessageDto;
import millimeeter.server.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  @Query(
      "SELECT new millimeeter.server.dto.MessageDto(m.id, m.senderId, m.content, m.parentMessageId,"
          + " m.senderReaction, m.receiverReaction, m.status, m.sentAt, m.seenAt) FROM messages m"
          + " WHERE m.matchId = ?1 ORDER BY m.sentAt DESC")
  List<MessageDto> findMessagesByMatchId(Long id);

  @Modifying
  @Transactional
  @Query(
      "UPDATE messages m SET m.status = 'SEEN', m.seenAt = CURRENT_TIMESTAMP "
          + "WHERE m.senderId <> ?1 AND m.matchId = ?2 AND m.seenAt IS NULL")
  void readMessagesInConversationByProfileIdAndMatchId(Long profileId, Long matchId);

  @Modifying
  @Transactional
  @Query(
      "UPDATE messages m SET m.status = 'DELIVERED' WHERE m.matchId IN (SELECT mt.id FROM matches"
          + " mt WHERE mt.profileId1 = ?1 OR mt.profileId2 = ?1) AND m.status = 'SENT'")
  void setMessagesStatusAsDeliveredByProfileId(Long profileId);
}
