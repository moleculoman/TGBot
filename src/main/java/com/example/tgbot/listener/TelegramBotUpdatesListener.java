package com.example.tgbot.listener;

import com.example.tgbot.entity.NotificationTask;
import com.example.tgbot.service.NotificationTaskService;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private Logger logger = (Logger) LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    //Matcher-pattern
    private Pattern pattern = Pattern.compile(
            "(\\d{1,2}.\\d{1,2}.\\d{4} \\d{1,2}:\\d{2})\\s+([Аa-я\\d\\s.!,?])"
    );
    //Date time pattern for formatter
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    //Creating a connection
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskService service;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    //Processing telegram api requests
    @Override
    public int process(List<Update> updates) {
        try {
            updates.stream()
                    .filter(update -> update.message()!=null)
                    .forEach(update -> {
                        logger.info("Processing update: {}", update);
                        Message message = update.message();
                        Long chatId = message.chat().id();
                        String text = message.text();
                        //Response to the start command
                        if ("/start".equals(text)) {
                            sendMessage(chatId,"Добрый день! Отправьте свою задачу в ввиде \"31.12.2023 23:59 Сделать домашнюю работу\"");
                        } else if (text!=null) {
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()){
                                LocalDateTime dateTime = parseDateTime(matcher.group(1));
                                if (Objects.isNull(dateTime)){
                                    sendMessage(chatId, "Некорректный формат даты");
                                }else{
                                    String txt = matcher.group(2);
                                    NotificationTask task = new NotificationTask();
                                    task.setChatId(chatId);
                                    task.setMessage(text);
                                    task.setNotificationDateTime(dateTime);
                                    //Saving to database
                                    service.save(task);
                                    sendMessage(chatId, "Задача успешно добавлена");
                                }
                            }else{
                                sendMessage(chatId, "Неверный формат");
                            }
                        }
                    });
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    private void sendMessage(Long chatId, String text){
        SendMessage sendMessage = new SendMessage(chatId,text);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()){
            logger.error("Произошла ошибка отправки текста: {}", sendResponse.description());
        }
    }
    @Nullable
    private LocalDateTime parseDateTime(String dateTime){
        try {
            return LocalDateTime.parse(dateTime, formatter);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
