package org.example.ratelimit;

public interface IEmailRateLimitRule {

    boolean CanSendEmail(String userId);

    void RegisterEmailSent(String userId);

    void StartCleanScheduler();
}
