package com.example.dreamland.asynctask;

import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.dreamland.R;
import com.example.dreamland.database.Sleep;
import com.example.dreamland.database.SleepDao;

public class NonRecommImageAsyncTask extends AsyncTask<Sleep, Void, Sleep> {

    private SleepDao SleepDao;
    private ImageView imageView;
    private int id;
    private Sleep selSleep;
    int[] posImages = new int[]{
        R.drawable.pos1, R.drawable.pos2, R.drawable.pos3,
            R.drawable.pos4, R.drawable.pos5};

    public NonRecommImageAsyncTask(SleepDao SleepDao, ImageView imageView, int id) {
        this.SleepDao = SleepDao;
        this.imageView = imageView;
        this.id = id;
    }

    @Override
    protected Sleep doInBackground(Sleep... Sleeps) {
        selSleep = SleepDao.getSleepById(id);
        return null;
    }

    @Override
    protected void onPostExecute(Sleep sleep) {
        super.onPostExecute(sleep);
        imageView.setImageResource(posImages[selSleep.getNonRecommPos()]);
    }
}
