package com.ms.users.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter @Setter
public class UserPreference extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserProfile userProfile;

    private String preferenceKey;
    private String preferenceValue;
}