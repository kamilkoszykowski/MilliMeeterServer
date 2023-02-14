package millimeeter.server.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import millimeeter.server.controller.ProfileController;
import millimeeter.server.dto.MyProfileDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class MyProfileDtoModelAssembler
    implements RepresentationModelAssembler<MyProfileDto, EntityModel<MyProfileDto>> {

  @Override
  public EntityModel<MyProfileDto> toModel(MyProfileDto profile) {
    EntityModel<MyProfileDto> profileDtoModel = EntityModel.of(profile);
    profileDtoModel.add(linkTo(methodOn(ProfileController.class).update(null)).withRel("update"));
    profileDtoModel.add(
        linkTo(methodOn(ProfileController.class).deleteProfile()).withRel("delete"));

    return profileDtoModel;
  }
}
