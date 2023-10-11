package org.example;

import org.example.notification.Gateway;
import org.example.notification.NotificationService;
import org.example.ratelimit.*;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        try {

            IEmailRateLimitRule newsRule = new EmailRuleRateLimit(3, RuleType.ByMinute);
            IEmailRateLimitRule statusRule = new EmailRuleRateLimit(2, RuleType.ByMinute);
            IEmailRateLimitRule updateRule = new EmailRuleRateLimit(1, RuleType.ByHour);

            HashMap<String, IEmailRateLimitRule> rules = new HashMap<>();
            rules.put("news", newsRule);
            rules.put("status", statusRule);
            rules.put("update", updateRule);

            NotificationService service = new NotificationService(new Gateway(), rules);

            Integer emailsCount = 50000;
            Integer threadSleep = 10000;

            for (Integer i = 0; i < emailsCount; i++ ) {

                service.send("news", "user-1", "news A");
                service.send("status", "user-2", "news A");

                Thread.sleep(10000);

                service.send("update", "user-3", "news X - Update");
                service.send("update", "user-4", "news Y - Update");
                service.send("update", "user-3", "news X - Update");
                service.send("update", "user-4", "news Y - Update");

                Thread.sleep(threadSleep);
            }

        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }
}




