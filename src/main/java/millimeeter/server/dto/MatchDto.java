package millimeeter.server.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchDto {

  private Long id;
  private Long profileId;
  private String firstName;
  private String[] photos;
  private LocalDateTime matchedAt;
}
