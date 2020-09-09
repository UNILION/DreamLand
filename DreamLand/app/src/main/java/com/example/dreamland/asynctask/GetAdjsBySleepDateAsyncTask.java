package com.example.dreamland.asynctask;

import android.os.AsyncTask;
import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AdjustmentDao;

import java.util.List;

public class GetAdjsBySleepDateAsyncTask extends AsyncTask<Adjustment, Void, List<Adjustment>> {

    private AdjustmentDao AdjustmentDao;
    private String date;

    public GetAdjsBySleepDateAsyncTask(AdjustmentDao AdjustmentDao, String date) {
        this.AdjustmentDao = AdjustmentDao;
        this.date = date;
    }

    @Override
    protected List<Adjustment> doInBackground(Adjustment... Adjustments) {
        return AdjustmentDao.getAdjsBySleepDate(date);
    }
}
