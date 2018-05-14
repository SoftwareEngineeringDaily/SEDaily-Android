package com.koalatea.thehollidayinn.softwareengineeringdaily.downloads;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.File;

/**
 * Created by keithholliday on 10/15/17.
 */

public class MP3FileManager {
  public String getRootDirPath(Context context) {
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      File file = ContextCompat.getExternalFilesDirs(context.getApplicationContext(),
              null)[0];
      return file.getAbsolutePath();
    } else {
      return context.getApplicationContext().getFilesDir().getAbsolutePath();
    }
  }

  public String getFileNameFromUrl(String urlString) {
    return urlString.substring(urlString.lastIndexOf('/') + 1, urlString.length());
  }

  public File getFileFromUrl (String urlString, Context context) {
    return new File(context.getFilesDir(), getFileNameFromUrl(urlString));
  }

  public String getExternalFileString (String urlString, Context context) {
    return context.getFilesDir() + "/" + getFileNameFromUrl(urlString);
  }
}
