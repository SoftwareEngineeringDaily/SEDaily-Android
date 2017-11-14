package com.koalatea.thehollidayinn.softwareengineeringdaily.mediaui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodcastSessionStateManager;

/*
 * Created by keithholliday on 11/3/17.
 */

public class SpeedDialog  extends DialogFragment {
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    builder.setTitle(R.string.speed)
      .setItems(R.array.speed_options, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          PodcastSessionStateManager.getInstance().setCurrentSpeed(which);
        }
      });

    return builder.create();
  }
}
