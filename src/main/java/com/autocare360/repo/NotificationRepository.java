package com.autocare360.repo;

import com.autocare360.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    Long countByUserIdAndIsRead(Long userId, Boolean isRead);

    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}

