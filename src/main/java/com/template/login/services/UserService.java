package com.template.login.services;

import com.template.login.entities.User;
import com.template.login.exceptions.ResourceNotFoundException;
import com.template.login.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("Invalid User Id"));
    }


}
