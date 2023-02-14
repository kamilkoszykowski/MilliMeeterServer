package millimeeter.server.model;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import millimeeter.server.enums.MessageReaction;
import millimeeter.server.enums.MessageStatus;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "disappearing_messages")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
public class DisappearingMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sender_id")
  private Long senderId;

  @Column(name = "receiver_id")
  private Long receiverId;

  private String content;

  @Column(name = "parent_message_id")
  private Long parentMessageId;

  @Enumerated(EnumType.STRING)
  @Type(type = "pgsql_enum")
  @Column(name = "sender_reaction", columnDefinition = "message_reaction")
  private MessageReaction senderReaction;

  @Enumerated(EnumType.STRING)
  @Type(type = "pgsql_enum")
  @Column(name = "receiver_reaction", columnDefinition = "message_reaction")
  private MessageReaction receiverReaction;

  @Enumerated(EnumType.STRING)
  @Type(type = "pgsql_enum")
  @Column(columnDefinition = "message_reaction")
  private MessageStatus status;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "seen_at")
  private LocalDateTime seenAt;
}
