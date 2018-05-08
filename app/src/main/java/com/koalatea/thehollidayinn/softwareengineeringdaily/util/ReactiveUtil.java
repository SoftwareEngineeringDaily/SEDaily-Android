package com.koalatea.thehollidayinn.softwareengineeringdaily.util;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.util.Log;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodcastDetailActivity;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by keithholliday on 4/24/18.
 */

public class ReactiveUtil {
  public static DisposableObserver<Void> getEmptyObservable() {
    return new DisposableObserver<Void>() {
      @Override
      public void onComplete() {
      }
      @Override
      public void onError(Throwable e) {
//        Log.v(TAG, e.toString());
      }

      @Override
      public void onNext(Void posts) {
      }
    };
  }

  public static DisposableObserver<Post> toDetailObservable(Activity activity) {
    return new DisposableObserver<Post>() {
      @Override
      public void onComplete() {}
      @Override
      public void onError(Throwable e) {
//        Log.v(TAG, e.toString());
      }
      @Override
      public void onNext(Post post) {
        Intent intent = new Intent(activity, PodcastDetailActivity.class);
        intent.putExtra("POST_ID", post.get_id());

        // @TODO: shared element
//        val options = ActivityOptions
//                .makeSceneTransitionAnimation(this, androidRobotView, "robot")

        activity.startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
      }
    };
  }
}
