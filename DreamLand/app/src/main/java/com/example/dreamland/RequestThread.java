package com.example.dreamland;

import android.util.Log;

public class RequestThread extends Thread {

    private BluetoothService bluetoothService;
    private boolean isStop = false;

    public RequestThread(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    @Override
    public void run() {
        Log.d("BLT", "request 시작");
        while (!isStop) {
            bluetoothService.writeBLT3("request");
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("BLT", "request 종료");
    }
}
