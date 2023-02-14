package millimeeter.server.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import millimeeter.server.controller.SwipeController;
import millimeeter.server.dto.ProfileToSwipeDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ProfileToSwipeDtoModelAssembler
    implements RepresentationModelAssembler<ProfileToSwipeDto, EntityModel<ProfileToSwipeDto>> {

  @Override
  public EntityModel<ProfileToSwipeDto> toModel(ProfileToSwipeDto profile) {
    EntityModel<ProfileToSwipeDto> profileModel = EntityModel.of(profile);
    profileModel.add(
        linkTo(methodOn(SwipeController.class).swipe(profile.getId(), "LEFT"))
            .withRel("swipe left"));
    profileModel.add(
        linkTo(methodOn(SwipeController.class).swipe(profile.getId(), "RIGHT"))
            .withRel("swipe right"));

    return profileModel;
  }
}
