package millimeeter.server.controller;

import millimeeter.server.model.User;
import millimeeter.server.service.UserService;
import millimeeter.server.service.assembler.UserModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;
  private final UserModelAssembler userModelAssembler;

  @Autowired
  public UserController(UserService userService, UserModelAssembler userModelAssembler) {
    this.userService = userService;
    this.userModelAssembler = userModelAssembler;
  }

  @PostMapping
  public ResponseEntity<EntityModel<User>> create() {
    return new ResponseEntity<>(
        userModelAssembler.toModel(userService.create()), HttpStatus.CREATED);
  }
}
