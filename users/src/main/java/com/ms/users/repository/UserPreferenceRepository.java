package com.ms.users.repository;

import com.ms.users.entity.UserPreference;
import com.ms.users.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUserProfile(UserProfile userProfile);
}
