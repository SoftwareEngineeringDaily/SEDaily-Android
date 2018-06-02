package com.koalatea.thehollidayinn.softwareengineeringdaily.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;

/**
 * Created by keithholliday on 4/24/18.
 */

public class AlertUtil {
  public static void displayMessage (Context context, String message) {
    AlertDialog.Builder builder;

    // Ensure context is active
    if ( context instanceof Activity ) {
      Activity activity = (Activity) context;
      if (activity.isFinishing()) {
        return;
      }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
    } else {
      builder = new AlertDialog.Builder(context);
    }

    builder.setTitle("Error")
      .setMessage(message)
      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {}
      })
      .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {}
      })
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show();
  }
}
