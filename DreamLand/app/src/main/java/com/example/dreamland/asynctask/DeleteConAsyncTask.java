package com.example.dreamland.asynctask;

import android.os.AsyncTask;

import com.example.dreamland.database.Condition;
import com.example.dreamland.database.ConditionDao;

public class DeleteConAsyncTask extends AsyncTask<Condition, Void, Void> {

    private ConditionDao conditionDao;

    public DeleteConAsyncTask(ConditionDao conditionDao) {
        this.conditionDao = conditionDao;
    }

    @Override
    protected Void doInBackground(Condition... conditions) {
        conditionDao.deleteAll();
        return null;
    }
}
