package millimeeter.server.dto;

import java.time.LocalDate;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDto {

  @NotBlank(message = "The first name is required")
  @Pattern(
      regexp = "[A-Z][a-z]{1,19}",
      message =
          "The first name must be a text starting with a big letter, not shorter than 2 characters"
              + " and not longer than 20 characters")
  private String firstName;

  @NotNull(message = "The date of birth is required")
  @Past(message = "The date of birth must be any valid date after 1900-01-01")
  private LocalDate dateOfBirth;

  @NotNull(message = "The gender is required")
  @Pattern(regexp = "MAN|WOMAN", message = "The gender value be a MAN or WOMAN")
  private String gender;

  @Size(max = 500, message = "The description can't be longer than 500 characters")
  private String description;

  @NotBlank(message = "My song required")
  @Pattern(regexp = "[a-zA-Z0-9]*", message = "My song is invalid")
  private String mySong;

  @NotNull(message = "The last latitude is required")
  @Min(value = -90, message = "The latitude must be between -90 and 90")
  @Max(value = 90, message = "The latitude must be between -90 and 90")
  private Double lastLatitude;

  @NotNull(message = "The last longitude is required")
  @Min(value = -90, message = "The longitude must be between -180 and 180")
  @Max(value = 90, message = "The longitude must be between -180 and 180")
  private Double lastLongitude;

  @NotNull(message = "The looking for is required")
  @Pattern(regexp = "MEN|WOMEN|BOTH", message = "Looking for value must be MEN, WOMEN or BOTH")
  private String lookingFor;

  @NotNull(message = "The search distance is required")
  @Min(value = 1, message = "The search distance must be between 1 and 100")
  @Max(value = 100, message = "The search distance must be between 1 and 100")
  private Integer searchDistance;

  @NotNull(message = "The age range minimum is required")
  @Min(value = 18, message = "The minimum age must be between 18 and 100")
  @Max(value = 100, message = "The minimum age must be between 18 and 100")
  private Integer ageRangeMinimum;

  @NotNull(message = "The age range maximum is required")
  @Min(value = 18, message = "The maximum age must be between 18 and 100")
  @Max(value = 100, message = "The maximum age must be between 18 and 100")
  private Integer ageRangeMaximum;
}
