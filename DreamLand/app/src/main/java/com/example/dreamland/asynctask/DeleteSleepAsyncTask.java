package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Sleep;
import com.example.dreamland.database.SleepDao;

public class DeleteSleepAsyncTask extends AsyncTask<Sleep, Void, Void> {

    private SleepDao sleepDao;

    public DeleteSleepAsyncTask(SleepDao sleepDao) {
        this.sleepDao = sleepDao;
    }

    @Override
    protected Void doInBackground(Sleep... sleeps) {
        sleepDao.deleteAll();
        return null;
    }
}
