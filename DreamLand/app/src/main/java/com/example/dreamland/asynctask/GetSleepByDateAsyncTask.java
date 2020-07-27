package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Sleep;
import com.example.dreamland.database.SleepDao;

import java.util.List;

public class GetSleepByDateAsyncTask extends AsyncTask<Sleep, Void, Sleep> {

    private SleepDao sleepDao;
    private String date;

    public GetSleepByDateAsyncTask(SleepDao sleepDao, String date) {
        this.sleepDao = sleepDao;
        this.date = date;
    }

    @Override
    protected Sleep doInBackground(Sleep... sleeps) {
        return sleepDao.getSleepByDate(date);
    }
}
