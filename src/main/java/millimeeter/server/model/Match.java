package millimeeter.server.model;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "matches")
public class Match {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "profile_id_1")
  private Long profileId1;

  @Column(name = "profile_id_2")
  private Long profileId2;

  @Column(name = "matched_at")
  private LocalDateTime matchedAt;

  public Match(Long profileId1, Long profileId2) {
    this.profileId1 = profileId1;
    this.profileId2 = profileId2;
    this.matchedAt = LocalDateTime.now();
  }
}
