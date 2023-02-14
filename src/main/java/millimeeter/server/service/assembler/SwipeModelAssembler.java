package millimeeter.server.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import millimeeter.server.controller.ProfileController;
import millimeeter.server.model.Swipe;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SwipeModelAssembler
    implements RepresentationModelAssembler<Swipe, EntityModel<Swipe>> {

  @Override
  public EntityModel<Swipe> toModel(Swipe swipe) {
    EntityModel<Swipe> swipeModel = EntityModel.of(swipe);
    swipeModel.add(
        linkTo(methodOn(ProfileController.class).getSwipesLeftCount())
            .withRel("swipes left amount"));

    return swipeModel;
  }
}
