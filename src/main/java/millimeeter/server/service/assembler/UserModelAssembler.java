package millimeeter.server.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import millimeeter.server.controller.ProfileController;
import millimeeter.server.model.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

  @Override
  public EntityModel<User> toModel(User user) {
    EntityModel<User> userModel = EntityModel.of(user);
    userModel.add(
        linkTo(methodOn(ProfileController.class).create(null, null)).withRel("create profile"));

    return userModel;
  }
}
