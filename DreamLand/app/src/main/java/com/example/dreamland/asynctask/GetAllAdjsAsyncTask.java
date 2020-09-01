package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AdjustmentDao;

import java.util.List;

public class GetAllAdjsAsyncTask extends AsyncTask<Adjustment, Void, List<Adjustment>> {

    private AdjustmentDao AdjustmentDao;

    public GetAllAdjsAsyncTask(AdjustmentDao AdjustmentDao) {
        this.AdjustmentDao = AdjustmentDao;
    }

    @Override
    protected List<Adjustment> doInBackground(Adjustment... Adjustments) {
        return AdjustmentDao.getAll();
    }
}
