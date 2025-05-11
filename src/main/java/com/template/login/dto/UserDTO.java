package com.template.login.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

    private String userId;

    @NotBlank(message = "Please enter a valid name")
    private String name;

    @NotBlank(message = "Please enter an email address")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Please enter a password")
    @Size(min = 5, max = 8, message = "Please enter a password between 5 and 8 characters")
    private String password;

    @NotBlank(message = "Please enter a valid address")
    private String address;
}
