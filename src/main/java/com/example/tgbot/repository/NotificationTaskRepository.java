package com.example.tgbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.tgbot.entity.NotificationTask;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask,Long> {
    List<NotificationTask> findAllByNotificationDateTime(LocalDateTime localDateTime);
}