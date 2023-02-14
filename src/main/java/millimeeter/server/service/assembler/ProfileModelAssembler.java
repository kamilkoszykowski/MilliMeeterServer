package millimeeter.server.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import millimeeter.server.controller.ProfileController;
import millimeeter.server.model.Profile;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ProfileModelAssembler
    implements RepresentationModelAssembler<Profile, EntityModel<Profile>> {

  @Override
  public EntityModel<Profile> toModel(Profile profile) {
    EntityModel<Profile> profileModel = EntityModel.of(profile);
    profileModel.add(linkTo(methodOn(ProfileController.class).update(null)).withRel("update"));
    profileModel.add(linkTo(methodOn(ProfileController.class).deleteProfile()).withRel("delete"));

    return profileModel;
  }
}
