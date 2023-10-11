package org.example.notification;

import java.util.HashMap;
import org.example.ratelimit.*;

public class NotificationService implements INotificationService {

    private Gateway gateway;
    private HashMap<String, IEmailRateLimitRule> emailRateLimitRules;

    public NotificationService(Gateway gateway, HashMap<String, IEmailRateLimitRule> rules ) throws Exception {

        this.gateway = gateway;

        if(rules.isEmpty())
            throw new Exception("Rules cannot be empty");

        this.emailRateLimitRules = rules;

        System.out.println("Notification Service - Starting cleaning schedulers");
        this.emailRateLimitRules.values().stream().forEach((r) -> {
            r.StartCleanScheduler();
        });
    }

    @Override
    public void send(String emailType, String userId, String message) {

        synchronized (this) {

            IEmailRateLimitRule ruleChecker = emailRateLimitRules.get(emailType);

            if (ruleChecker.CanSendEmail(userId)) {
                gateway.send(userId, message);
                ruleChecker.RegisterEmailSent(userId);
            } else {
                System.out.println("**** The email cannot be sent. Rate Limit at maximum capacity for: User " + userId + " - Email Type: " + emailType);
            }
        }
    }

}
