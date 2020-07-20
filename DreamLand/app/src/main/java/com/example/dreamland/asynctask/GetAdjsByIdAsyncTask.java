package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AdjustmentDao;

public class GetAdjsByIdAsyncTask extends AsyncTask<Adjustment, Void, Adjustment[]> {

    AdjustmentDao AdjustmentDao;
    int id;

    public GetAdjsByIdAsyncTask(AdjustmentDao AdjustmentDao, int id) {
        this.AdjustmentDao = AdjustmentDao;
        this.id = id;
    }

    @Override
    protected Adjustment[] doInBackground(Adjustment... Adjustments) {
        return AdjustmentDao.getAdjsById(id);
    }
}
