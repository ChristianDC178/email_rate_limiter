# Email Rate Limiter

## Rules 

There is a requirement to send marketing emails with different categories and periodicity to all of our users. For example an Status email should send twice by minute to each user. As we know, this could cause a lot of spam in the email inbox. 
In addition, to avoid the spam situation we need to build a Email Rate Limiter and must allow the following example configurations: 

| Email Category|  Periodicity  | Threshold by user  |
| ------------- |:-------------:| -------------      |
| Status        | `By Minute`   | 2                  |
| News          | `By Hour`     | 3                  |
| Update        | `By Day`      | 1                  |

etc..

## Code and How it works

- A rate limit rule is represented by an `EmailRuleRateLimit`. This can be configured with `RuleType` enum ( Minute, Hour, Day) and a threshold, that represents a quantity of emails able to send in the period. 

```java
 IEmailRateLimitRule newsRule = new EmailRuleRateLimit(3, RuleType.ByMinute);
 IEmailRateLimitRule statusRule = new EmailRuleRateLimit(2, RuleType.ByMinute);
 IEmailRateLimitRule updateRule = new EmailRuleRateLimit(1, RuleType.ByHour);
```

-  The `NotificationService` class uses a collection of `EmailRuleRateLimit` passed by constructor.  

```java
 HashMap<String, IEmailRateLimitRule> rules = new HashMap<>();
            rules.put("news", newsRule);
            rules.put("status", statusRule);
            rules.put("update", updateRule);

 NotificationService service = new NotificationService(new Gateway(), rules);
```

- When the service tries to send an email, it checks first the rule regarding the email category and then that rule has the logic to know if it is possible to send.

```java 
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
```

-  If an email is send, the rule save a log in a `UserEmailLog` class instance.

```java 
 public void RegisterEmailSent(String userId) {
        long emailSentAt = ruleType == RuleType.ByDay ? LocalDate.now().toEpochDay() : System.currentTimeMillis();
        UserEmailLog eLog = this.emailLogs.get(userId);
        if (eLog == null) {
            eLog = new UserEmailLog(emailSentAt);
            this.emailLogs.put(userId, eLog);
        } else {
            eLog.update(emailSentAt);
        }
 }
```

- More things about the `EmailRuleRateLimit` class. It has a background scheduler process that looks for email logs expired and removes it from it his log collection. This allow to reset the rate limit for each user.  

```java 
 public void StartCleanScheduler() {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {

            synchronized (this.emailLogs) {

                Iterator<Map.Entry<String, UserEmailLog>> iterator = this.emailLogs.entrySet().iterator();

                System.out.println("@@@ Running Clean Scheduler " + this.emailLogs.size());

                // Iterate through the HashMap and remove entries containing "Remove"
                while (iterator.hasNext()) {

                    UserEmailLog eLog = iterator.next().getValue();
                    long result = System.currentTimeMillis() - eLog.getEmailSentDateTime();

                    if (ruleType == RuleType.ByMinute && (result > Constants.MINUTE_IN_MILLISECONDS)) {
                        System.out.println("### ByMinute - Message Cleaned");
                        iterator.remove();
                    } else if (ruleType == RuleType.ByHour && (result > Constants.HOUR_IN_MILLISECONDS)) {
                        System.out.println("### ByHour - Message Cleaned");
                        iterator.remove();
                    } else if (ruleType == RuleType.ByDay && (LocalDate.now().toEpochDay() > eLog.getEmailSentDateTime())) {
                        System.out.println("### ByDay - Message Cleaned");
                        iterator.remove();
                    }

                }
            }

        }, 0, Constants.SCHEDULER_WAIT, translateToTimeUnit(ruleType));
    }
```

- Class Diagram 

![Class Diagram](https://github.com/ChristianDC178/email_rate_limiter/blob/main/class_diagram.jpg "Class Diagram")
