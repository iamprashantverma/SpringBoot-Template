package com.template.login.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Session> sessions = new HashSet<>();


}
