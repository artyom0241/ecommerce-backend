package com.ecommercebackend.api.controller.auth;

import com.ecommercebackend.api.model.LoginBody;
import com.ecommercebackend.api.model.LoginResponse;
import com.ecommercebackend.api.model.RegistrationBody;
import com.ecommercebackend.exception.EmailFailureException;
import com.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommercebackend.model.LocalUser;
import com.ecommercebackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/auth", method = RequestMethod.GET)
public class AuthenticationController {


  private UserService userService;


  public AuthenticationController(UserService userService) {
    this.userService = userService;
  }


  @PostMapping("/register")
  public ResponseEntity registerUser(@Valid @RequestBody RegistrationBody registrationBody) {
    try {
      userService.registerUser(registrationBody);
      return ResponseEntity.ok().build();
    } catch (UserAlreadyExistsException ex) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (EmailFailureException e) {
        throw new RuntimeException(e);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody) {
    String jwt = userService.loginUser(loginBody);
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } else {
      LoginResponse response = new LoginResponse();
      response.setJwt(jwt);
      return ResponseEntity.ok(response);
    }
  }


  @GetMapping("/me")
  public LocalUser getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user) {
    return user;
  }

}
