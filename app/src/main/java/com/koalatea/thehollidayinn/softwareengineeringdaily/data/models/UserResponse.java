package com.koalatea.thehollidayinn.softwareengineeringdaily.data.models;

/**
 * Created by keithholliday on 1/6/18.
 */

public class UserResponse {
    SubscriptionResponse subscription;

    public SubscriptionResponse getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionResponse subscription) {
        this.subscription = subscription;
    }
}
