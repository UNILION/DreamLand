package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Condition;
import com.example.dreamland.database.ConditionDao;

import java.util.List;

public class GetConsBySleepDateAsyncTask extends AsyncTask<Condition, Void, List<Condition>> {

    private ConditionDao ConditionDao;
    private String date;

    public GetConsBySleepDateAsyncTask(ConditionDao ConditionDao, String date) {
        this.ConditionDao = ConditionDao;
        this.date = date;
    }

    @Override
    protected List<Condition> doInBackground(Condition... Conditions) {
        return ConditionDao.getConsBySleepDate(date);
    }
}
