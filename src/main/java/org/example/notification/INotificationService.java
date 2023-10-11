package org.example.notification;

public interface INotificationService {

    void send(String type, String userId, String message);

}