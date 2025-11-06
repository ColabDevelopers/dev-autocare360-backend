package com.autocare360.repo;

import com.autocare360.entity.NotificationPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, Long> {

  Optional<NotificationPreference> findByUserId(Long userId);

  boolean existsByUserId(Long userId);
}
