package com.koalatea.thehollidayinn.softwareengineeringdaily.downloads;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.PodcastDownloadsRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by keithholliday on 10/15/17.
 */

public class DownloadTask extends AsyncTask<String, Integer, String> {

  private Context context;
  private final int id = 1;
  private PowerManager.WakeLock mWakeLock;
  private ProgressDialog mProgressDialog;
  private NotificationManager mNotifyManager;
  private NotificationCompat.Builder mBuilder;
  private String podcastId;

  public DownloadTask(Context context, NotificationManager notificationManager, NotificationCompat.Builder mBuilder, String mPodcastId) {
    this.context = context;
    this.mProgressDialog = mProgressDialog;
    this.mNotifyManager = notificationManager;
    this.mBuilder = mBuilder;
    this.podcastId = mPodcastId;
  }

  @Override protected String doInBackground(String... sUrl) {
    InputStream input = null;
    FileOutputStream output = null;
    HttpURLConnection connection = null;

    try {
      URL url = new URL(sUrl[0]);
      connection = (HttpURLConnection) url.openConnection();
      connection.connect();

      // expect HTTP 200 OK, so we don't mistakenly save error report
      // instead of the file
      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        return "Server returned HTTP " + connection.getResponseCode()
            + " " + connection.getResponseMessage();
      }

      // this will be useful to display download percentage
      // might be -1: server did not report the length
      int fileLength = connection.getContentLength();

      // download the file
      input = connection.getInputStream();

      String urlString = url.toString();
      output = new FileOutputStream(new MP3FileManager().getFileFromUrl(urlString, context.getApplicationContext()));

      byte data[] = new byte[4096];
      long total = 0;
      int count;
      while ((count = input.read(data)) != -1) {
        // allow canceling with back button
        if (isCancelled()) {
          input.close();
          return null;
        }
        total += count;
        // publishing the progress....
        if (fileLength > 0) // only if total length is known
          publishProgress((int) (total * 100 / fileLength));
        output.write(data, 0, count);
      }
    } catch (Exception e) {
      return e.toString();
    } finally {
      try {
        if (output != null) output.close();
        if (input != null) input.close();
      } catch (IOException ignored) {
      }

      if (connection != null) connection.disconnect();
    }
    return null;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    // take CPU lock to prevent CPU from going off if the user
    // presses the power button during download
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        getClass().getName());
    mWakeLock.acquire();

    //mProgressDialog.show();
    mBuilder.setProgress(0, 0, true);
    mNotifyManager.notify(id, mBuilder.build());
  }

  @Override
  protected void onProgressUpdate(Integer... progress) {
    super.onProgressUpdate(progress);
    // if we get here, length is known, now set indeterminate to false
    //mProgressDialog.setIndeterminate(false);
    //mProgressDialog.setMax(100);
    //mProgressDialog.setProgress(progress[0]);
    //
    //mBuilder.setProgress(100, progress[0], false);
    //
    //Notification notification = mBuilder.build();
    //notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
    //
    //mNotifyManager.notify(id, mBuilder.build());
  }

  @Override
  protected void onPostExecute(String result) {
    mWakeLock.release();

    mBuilder.setContentText("Download complete")
        .setProgress(0,0,false);
    mNotifyManager.notify(id, mBuilder.build());

    PodcastDownloadsRepository.getInstance().setPodcastDownload(podcastId);

    if (result != null)
      Log.v("keithtest2", String.valueOf("Download error: "+result));
    else
      Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
  }

}
