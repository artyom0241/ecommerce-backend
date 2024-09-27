package com.ecommercebackend.service;

import com.ecommercebackend.api.model.RegistrationBody;
import com.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommercebackend.model.LocalUser;
import com.ecommercebackend.model.dao.LocalUserDAO;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private LocalUserDAO localUserDAO;


    public UserService(LocalUserDAO localUserDAO) {
        this.localUserDAO = localUserDAO;
    }



    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {

        if (localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        LocalUser user = new LocalUser();
        user.setUsername(registrationBody.getUsername());
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        //TODO: Encrypt password!!
        user.setPassword(registrationBody.getPassword());
        return localUserDAO.save(user);
    }
}
