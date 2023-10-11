package org.example.ratelimit;

 final class UserEmailLog {

    private Long emailSentDateTime;
    private Integer emailCounter;
    
    public UserEmailLog(Long milliseconds) {
        this.emailSentDateTime = milliseconds;
        this.emailCounter = 1;
    }

    public Long getEmailSentDateTime() {
        return emailSentDateTime;
    }

    public Integer getEmailCounter() {
        return emailCounter;
    }


    public void update(Long milliseconds) {
        this.emailSentDateTime = milliseconds;
        this.emailCounter += 1;
    }

}
