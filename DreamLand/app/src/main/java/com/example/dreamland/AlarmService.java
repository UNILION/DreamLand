package com.example.dreamland;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AlarmService extends Service {
    public AlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AlarmTest", "on start commmand");
        Toast.makeText(this, "일어날 시간", Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
    }
}
