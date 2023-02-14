package millimeeter.server.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LocationDto {

  @NotNull(message = "The last latitude is required")
  @Min(value = -90, message = "The latitude must be between -90 and 90")
  @Max(value = 90, message = "The latitude must be between -90 and 90")
  private Double lastLatitude;

  @NotNull(message = "The last longitude is required")
  @Min(value = -90, message = "The longitude must be between -180 and 180")
  @Max(value = 90, message = "The longitude must be between -180 and 180")
  private Double lastLongitude;
}
