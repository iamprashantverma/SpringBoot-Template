package com.template.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private String
    private String token;
    private String tokenType = "Bearer";
    private String userId;
    private String email;

}
