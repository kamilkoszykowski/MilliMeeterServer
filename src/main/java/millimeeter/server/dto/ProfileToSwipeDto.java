package millimeeter.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import millimeeter.server.enums.Gender;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileToSwipeDto {

  private Long id;
  private String firstName;
  private Integer age;
  private Gender gender;
  private String[] photos;
  private String description;
  private String mySong;
  private Integer distanceAway;
}
