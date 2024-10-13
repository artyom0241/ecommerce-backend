package com.ecommercebackend.api.controller.user;

import com.ecommercebackend.api.model.DataChange;
import com.ecommercebackend.model.Address;
import com.ecommercebackend.model.LocalUser;
import com.ecommercebackend.model.dao.AddressDAO;
import com.ecommercebackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    /** The Address DAO. */
    private AddressDAO addressDAO;
    private SimpMessagingTemplate simpMessagingTemplate;
    private UserService userService;

    /**
     * Constructor for spring injection.
     * @param addressDAO
     */
    public UserController(AddressDAO addressDAO, SimpMessagingTemplate simpMessagingTemplate, UserService userService) {
        this.addressDAO = addressDAO;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(
            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId) {
        if (!userService.userHasPermissionToUser(userId, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressDAO.findByUser_Id(userId));
    }

    @PutMapping("/{userId}/address")
    public ResponseEntity<Address> putAddress(
            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId,
            @RequestBody Address address) {
        if (!userService.userHasPermissionToUser(userId, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        LocalUser refUser = new LocalUser();
        refUser.setId(userId);
        address.setUser(refUser);
        Address savedAddress = addressDAO.save(address);
        simpMessagingTemplate.convertAndSend("/topic/user" + userId  + "/address",
                new DataChange<>(DataChange.ChangeType.INSERT, address));
        return ResponseEntity.ok(savedAddress);
    }

    @PatchMapping("/{userId}/address/{addressId}")
    public ResponseEntity<Address> pathAddress(
            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId,
            @PathVariable Long addressId, @RequestBody Address address) {
        if (!userService.userHasPermissionToUser(userId, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (Objects.equals(address.getId(), addressId)) {
            Optional<Address> opOriginalAddress = addressDAO.findById(addressId);
            if (opOriginalAddress.isPresent()) {
                LocalUser originalUser = opOriginalAddress.get().getUser();
                if (originalUser.getId().equals(userId)) {
                    address.setUser(originalUser);
                    Address savedAddress = addressDAO.save(address);
                    simpMessagingTemplate.convertAndSend("/topic/user" + userId  + "/address",
                            new DataChange<>(DataChange.ChangeType.UPDATE, address));
                    return ResponseEntity.ok(savedAddress);
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }


}
