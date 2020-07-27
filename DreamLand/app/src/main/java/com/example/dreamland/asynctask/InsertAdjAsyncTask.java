package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AdjustmentDao;

public class InsertAdjAsyncTask extends AsyncTask<Adjustment, Void, Void> {

    private AdjustmentDao adjustmentDao;

    public InsertAdjAsyncTask(AdjustmentDao adjustmentDao) {
        this.adjustmentDao = adjustmentDao;
    }

    @Override
    protected Void doInBackground(Adjustment... adjustments) {
        adjustmentDao.insert(adjustments[0]);
        return null;
    }
}
