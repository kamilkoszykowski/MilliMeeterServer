package millimeeter.server.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import millimeeter.server.enums.Gender;
import millimeeter.server.enums.LookingFor;
import millimeeter.server.model.Profile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyProfileDto {

  private String firstName;
  private LocalDate dateOfBirth;
  private Gender gender;
  private List<String> photos;
  private String description;
  private String mySong;
  private LookingFor lookingFor;
  private Integer searchDistance;
  private Integer ageRangeMinimum;
  private Integer ageRangeMaximum;

  public MyProfileDto(Profile profile) {
    this.firstName = profile.getFirstName();
    this.dateOfBirth = profile.getDateOfBirth();
    this.gender = profile.getGender();
    this.photos = profile.getPhotos();
    this.description = profile.getDescription();
    this.mySong = profile.getMySong();
    this.lookingFor = profile.getLookingFor();
    this.searchDistance = profile.getSearchDistance();
    this.ageRangeMinimum = profile.getAgeRangeMinimum();
    this.ageRangeMaximum = profile.getAgeRangeMaximum();
  }
}
