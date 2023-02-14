package millimeeter.server.service;

import millimeeter.server.dto.LocationDto;
import millimeeter.server.dto.RegistrationDto;
import millimeeter.server.dto.UpdatedProfileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
  ResponseEntity<Object> create(RegistrationDto registrationDto, MultipartFile photos[]);

  ResponseEntity<Object> update(UpdatedProfileDto updatedProfileDto);

  ResponseEntity<Object> uploadPhoto(int index, MultipartFile photo);

  ResponseEntity<Object> findProfilesToSwipe();

  ResponseEntity<Object> getSwipesLeftCount();

  ResponseEntity<Void> deleteMyProfile();

  ResponseEntity<Object> deletePhoto(int index);

  ResponseEntity<Object> updateLocation(LocationDto location);

  ResponseEntity<Object> getMyProfile();
}
