package com.ms.users.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class UserProfile extends BaseEntity {
    @Id
    private String keycloakId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"))
    private Set<String> roles = new HashSet<>();

    @Column(name = "email_verified")
    private boolean emailVerified;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL)
    private Set<UserPreference> preferences = new HashSet<>();
}
