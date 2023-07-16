package com.example.tgbot.service;

import com.example.tgbot.entity.NotificationTask;
import com.example.tgbot.repository.NotificationTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationTaskService {
    @Autowired
    NotificationTaskRepository notificationTaskRepository;
    public void save(NotificationTask task){
        notificationTaskRepository.save(task);
    }

}