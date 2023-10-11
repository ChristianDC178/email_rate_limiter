# Email Rate Limiter

## Rules 

There is a requirement to send marketing emails with different categories and periodicity to all of our users. For example an Status email should send twice by minute to each user. As we know, this could cause a lot of spam in the email inbox. 
In addition, to avoid the spam situation we need to build a Email Rate Limiter and must allow the following example configuration: 

| Email Category|  Periodicity  | Threshold by user  |
| ------------- |:-------------:| -------------      |
| Status        | `By Minute`   | 2                  |
| News          | `By Hour`     | 3                  |
| Update        | `By Day`      | 1                  |

etc..

## Code and How it works

- A rate limit rule is represented by an `EmailRuleRateLimit`. This can be configured with `RuleType` enum ( Minute, Hour, Day) and a threshold, that represents que quantity of emails able to send in the period. 

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

- When the service tries to send an email, first check the rule regarding the email category 



