package millimeeter.server.model;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import millimeeter.server.enums.SwipeDirection;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "swipes")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
public class Swipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sender_id")
  private Long senderId;

  @Column(name = "receiver_id")
  private Long receiverId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "direction")
  @Type(type = "pgsql_enum")
  private SwipeDirection direction;

  @Column(name = "swiped_at")
  private LocalDateTime swipedAt;

  public Swipe(Long senderId, Long receiverId, String direction) {
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.direction = SwipeDirection.valueOf(direction);
    this.swipedAt = LocalDateTime.now();
  }
}
