package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AdjustmentDao;
import com.example.dreamland.database.Sleep;
import com.example.dreamland.database.SleepDao;

public class DeleteAdjAsyncTask extends AsyncTask<Adjustment, Void, Void> {

    private AdjustmentDao adjustmentDao;

    public DeleteAdjAsyncTask(AdjustmentDao adjustmentDao) {
        this.adjustmentDao = adjustmentDao;
    }

    @Override
    protected Void doInBackground(Adjustment... adjustments) {
        adjustmentDao.deleteAll();
        return null;
    }
}
