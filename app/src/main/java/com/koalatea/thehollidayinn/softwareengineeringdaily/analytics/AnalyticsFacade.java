package com.koalatea.thehollidayinn.softwareengineeringdaily.analytics;

import android.support.annotation.NonNull;

/**
 * Facade to capture events for use in any analytics processing
 * Created by Kurian on 20-Oct-17.
 */
public interface AnalyticsFacade {

  /**
   * Track an up-vote event
   * @param postId Id of the post that's been up-voted
   */
  void trackUpVote(@NonNull String postId);

  /**
   * Track an down-vote event
   * @param postId Id of the post that's been down-voted
   */
  void trackDownVote(@NonNull String postId);

  /**
   * Track a user registration event
   * @param username the username that has been registered
   */
  void trackRegistration(@NonNull String username);

  /**
   * Track a user login event
   * @param username the username that has been logged in
   */
  void trackLogin(@NonNull String username);
}
