package com.template.login.advices;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Builder
@Data
public class APIError {
    private HttpStatus status;
    private String message ;
}
