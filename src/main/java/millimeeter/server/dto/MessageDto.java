package millimeeter.server.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import millimeeter.server.enums.MessageReaction;
import millimeeter.server.enums.MessageStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

  private Long id;
  private Long senderId;
  private String content;
  private Long parentMessageId;
  private MessageReaction senderReaction;
  private MessageReaction receiverReaction;
  private MessageStatus status;
  private LocalDateTime sentAt;
  private LocalDateTime seenAt;
}
