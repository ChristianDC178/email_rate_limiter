package org.example.ratelimit;

import java.util.HashMap;

public abstract class EmailRuleRateLimitBase implements IEmailRateLimitRule {

    protected HashMap<String, UserEmailLog> emailLogs = new HashMap<>();
    protected final Integer emailThreshold;

    public EmailRuleRateLimitBase(Integer emailThreshold) {
        this.emailThreshold = emailThreshold;
    }

}
