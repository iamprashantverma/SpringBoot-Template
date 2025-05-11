package com.template.login.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Data;

import static com.template.login.util.UserUtil.generateUserId;

@Entity
@Data
public class User {

    @Id
    private String userId;

    @PrePersist
    private void setUserId(){
        this.userId = generateUserId();
    }

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String address;

}
