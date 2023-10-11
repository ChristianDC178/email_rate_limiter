package org.example.notification;

import java.util.HashMap;
import org.example.ratelimit.*;

public class NotificationService implements INotificationService {

    private Gateway gateway;

    private HashMap<String, IEmailRateLimitRule> ruleCheckers;

    public NotificationService(Gateway gateway, HashMap<String, IEmailRateLimitRule> ruleCheckers ) throws Exception {
        this.gateway = gateway;

        if(ruleCheckers.isEmpty())
            throw new Exception("Rules cannot be empty");

        this.ruleCheckers = ruleCheckers;

    }

    @Override
    public void send(String emailType, String userId, String message) {

        synchronized (this) {

            IEmailRateLimitRule ruleChecker = ruleCheckers.get(emailType);

            if (ruleChecker.CanSendEmail(userId)) {
                gateway.send(userId, message);
                ruleChecker.RegisterEmailSent(userId);
            } else {
                System.out.println("The email has not been sent " + userId);
            }
        }
    }

}
