package org.example.ratelimit;

import org.example.Constants;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

public class EmailRuleRateLimit extends EmailRuleRateLimitBase {

    RuleType ruleType;

    public EmailRuleRateLimit(Integer emailThreshold, RuleType ruleType) {
        super(emailThreshold);
        this.ruleType = ruleType;
    }

    private TimeUnit translateToTimeUnit(RuleType ruleType) {

        if (ruleType == RuleType.ByMinute) {
            return TimeUnit.MINUTES;
        }

        if (ruleType == RuleType.ByHour) {
            return TimeUnit.HOURS;
        }

        return TimeUnit.DAYS;

    }

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

    @Override
    public boolean CanSendEmail(String userId) {

        UserEmailLog eLog = this.emailLogs.get(userId);
        if (eLog == null) {
            return true;
        }

        switch (ruleType) {
            case ByMinute:
                if ((System.currentTimeMillis() - eLog.getEmailSentDateTime()) < Constants.MINUTE_IN_MILLISECONDS && eLog.getEmailCounter() < emailThreshold) {
                    return true;
                }
                break;
            case ByHour:
                if ((System.currentTimeMillis() - eLog.getEmailSentDateTime()) < Constants.HOUR_IN_MILLISECONDS && eLog.getEmailCounter() < emailThreshold) {
                    return true;
                }
                break;
            case ByDay:
                if ((LocalDate.now().toEpochDay() > eLog.getEmailSentDateTime()) && eLog.getEmailCounter() < emailThreshold) {
                    return true;
                }
                break;
        }

        return false;
    }

    @Override
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

}