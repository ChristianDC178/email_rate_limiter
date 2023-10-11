package org.example;

import org.example.notification.Gateway;
import org.example.notification.NotificationService;
import org.example.ratelimit.*;

import java.time.LocalDate;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        try {

            long epochDate = LocalDate.now().toEpochDay();





            IEmailRateLimitRule minuteChecker = new EmailRuleRateLimit(2);

            HashMap<String, IEmailRateLimitRule> rules = new HashMap<>();
            rules.put("news", minuteChecker);

            minuteChecker.StartCleanScheduler();

            NotificationService service = new NotificationService(new Gateway(), rules);
            service.send("news", "user-1", "news A");
            service.send("news", "user-1", "news B");
            service.send("news", "user-1", "news C");
            service.send("news", "user-1", "news D");
            service.send("news", "user-1", "news E");
            service.send("news", "user-1", "news F");
            service.send("news", "user-1", "news G");

            Thread.sleep(120000);

            service.send("news", "user-1", "news 2");
            service.send("news", "user-1", "news 2");

        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }
}




