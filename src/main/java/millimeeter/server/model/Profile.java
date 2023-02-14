package millimeeter.server.model;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import millimeeter.server.dto.RegistrationDto;
import millimeeter.server.enums.Gender;
import millimeeter.server.enums.LookingFor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "profiles")
@TypeDefs({
  @TypeDef(name = "list-array", typeClass = ListArrayType.class),
  @TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
})
public class Profile {

  @Id private Long id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "gender")
  @Type(type = "pgsql_enum")
  private Gender gender;

  @Type(type = "list-array")
  @Column(columnDefinition = "text[]")
  private List<String> photos;

  private String description;

  @Column(name = "my_song")
  private String mySong;

  @Column(name = "last_latitude")
  private Double lastLatitude;

  @Column(name = "last_longitude")
  private Double lastLongitude;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "swipes_left")
  private Integer swipesLeft;

  @Column(name = "wait_until")
  private LocalDateTime waitUntil;

  @Enumerated(EnumType.STRING)
  @Column(name = "looking_for", columnDefinition = "looking_for")
  @Type(type = "pgsql_enum")
  private LookingFor lookingFor;

  @Column(name = "search_distance")
  private Integer searchDistance;

  @Column(name = "age_range_minimum")
  private Integer ageRangeMinimum;

  @Column(name = "age_range_maximum")
  private Integer ageRangeMaximum;

  public Profile(Long id, RegistrationDto registrationDto, List<String> photosData) {
    this.id = id;
    this.firstName = registrationDto.getFirstName();
    this.dateOfBirth = registrationDto.getDateOfBirth();
    this.gender = Gender.valueOf(registrationDto.getGender());
    this.photos = photosData;
    this.description = registrationDto.getDescription();
    this.mySong = registrationDto.getMySong();
    this.lastLatitude = registrationDto.getLastLatitude();
    this.lastLongitude = registrationDto.getLastLongitude();
    this.createdAt = LocalDateTime.now();
    this.swipesLeft = 50;
    this.lookingFor = LookingFor.valueOf(registrationDto.getLookingFor());
    this.searchDistance = registrationDto.getSearchDistance();
    this.ageRangeMinimum = registrationDto.getAgeRangeMinimum();
    this.ageRangeMaximum = registrationDto.getAgeRangeMaximum();
  }
}
