package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Condition;
import com.example.dreamland.database.ConditionDao;

public class InsertConAsyncTask extends AsyncTask<Condition, Void, Void> {

    private ConditionDao conditionDao;

    public InsertConAsyncTask(ConditionDao conditionDao) {
        this.conditionDao = conditionDao;
    }

    @Override
    protected Void doInBackground(Condition... conditions) {
        conditionDao.insert(conditions[0]);
        return null;
    }
}
