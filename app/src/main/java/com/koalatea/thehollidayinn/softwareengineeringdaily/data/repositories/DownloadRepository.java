package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Download;

import java.util.List;

public class DownloadRepository {
  private DownloadDao downloadDao;

  public DownloadRepository() {
    AppDatabase db = AppDatabase.getDatabase();
    downloadDao = db.downloadDao();
  }

  public void insert(Download download) {
    new insertAsyncTask(downloadDao).execute(download);
  }

  public void remove(String podcastId) {
    new removeAsyncTask(downloadDao).execute(podcastId);
  }

  public List<Download> getDownloads() {
    return downloadDao.getAll();
  }

  private static class insertAsyncTask extends AsyncTask<Download, Void, Void> {
    private DownloadDao downloadAsyncDao;

    insertAsyncTask(DownloadDao dao) {
      downloadAsyncDao = dao;
    }

    @Override
    protected Void doInBackground(final Download... params) {
      downloadAsyncDao.insertOne(params[0]);
      return null;
    }
  }

  private static class removeAsyncTask extends AsyncTask<String, Void, Void> {
    private DownloadDao downloadAsyncDao;

    removeAsyncTask(DownloadDao dao) {
      downloadAsyncDao = dao;
    }

    @Override
    protected Void doInBackground(final String... params) {
      Download download = downloadAsyncDao.loadById(params[0]);
      if (download == null) return null;
      downloadAsyncDao.delete(download);
      return null;
    }
  }
}
