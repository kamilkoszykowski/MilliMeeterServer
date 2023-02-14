package millimeeter.server.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import millimeeter.server.enums.MessageStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchWithMessagesDto {

  private Long id;
  private Long profileId;
  private String firstName;
  private String[] photos;
  private Long senderId;
  private String lastMessageContent;
  private MessageStatus lastMessageStatus;
  private LocalDateTime lastMessageSentAt;
}
