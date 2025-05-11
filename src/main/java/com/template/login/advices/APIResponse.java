package com.template.login.advices;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import java.time.LocalDateTime;

@Data
public class APIResponse <T> {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeStamp;

    private  T data;
    private  APIError error ;

    APIResponse(){
        this.timeStamp = LocalDateTime.now();
    }

    public APIResponse(T data) {
        this() ;
        this.data = data;
    }

    public APIResponse(APIError error) {
        this();
        this.error = error;
    }

}
