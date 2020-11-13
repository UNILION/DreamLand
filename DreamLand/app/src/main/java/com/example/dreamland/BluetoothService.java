package com.example.dreamland;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import iammert.com.library.Status;
import iammert.com.library.StatusView;

public class BluetoothService {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static BluetoothService instance = new BluetoothService();
    private static final int NUM_OF_DEVICES = 3;
    private Context context;
    private Handler handler; // handler that gets info from Bluetooth service
    private BluetoothAdapter bluetoothAdapter;
    ConnectedThread[] connectedThreads;
    ArrayList<BluetoothSocket> bltSockets;
    int deviceCount;
    Handler mHandler;
    public boolean isConnected = false;

    public BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bltSockets = new ArrayList<>();
        connectedThreads = new ConnectedThread[3];  // 0: 엑추에이터, 1: 침대 센서, 2: 손목 밴드
        deviceCount = 0;
        mHandler = new Handler();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public static BluetoothService getInstance() {
        return instance;
    }

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    // 기기 연결 후 입출력 함수
    void connected(BluetoothSocket socket, BluetoothDevice device) {
        switch (device.getName()) {
            case "BLT1":
                connectedThreads[0] = new BluetoothService.ConnectedThread(socket);
                connectedThreads[0].start();
                break;
            case "BLT2":
                connectedThreads[1] = new BluetoothService.ConnectedThread(socket);
                connectedThreads[1].start();
                break;
            case "JCNET-JARDUINO-7826":
                connectedThreads[2] = new BluetoothService.ConnectedThread(socket);
                connectedThreads[2].start();
                break;
            default:
                Log.d(MainActivity.STATE_TAG, "이름이 일치하지 않음");
        }
    }

    // 기기 연결 함수
    void connect(BluetoothDevice device) {
        new ConnectThread(device).start();
    }

    // 기기 연결 해제 함수
    void cancel() {
        for (ConnectedThread thread : connectedThreads) {
            if (thread != null) {
                thread.cancel();
                deviceCount--;
                Log.d(MainActivity.STATE_TAG, "연결된 디바이스 수 -> " + deviceCount);
            }
        }
        if (deviceCount == 0) {  // 모두 연결 해제
            disconnectionCompleted();
            bltSockets = new ArrayList<>();
            connectedThreads = new ConnectedThread[3];
            Log.d(MainActivity.STATE_TAG, "모든 기기 연결 해제");
        }
    }

    // 연결이 모두 완료되면 호출
    void connectionCompleted() {
        isConnected = true;
        StatusView statusView = (StatusView) ((MainActivity)context).findViewById(R.id.status);
        statusView.setStatus(Status.COMPLETE);
        ((MainActivity) context).settingFragment.progressBar.setVisibility(View.GONE);
        ((MainActivity) context).settingFragment.conBtSwitch.setVisibility(View.VISIBLE);
        Log.d(MainActivity.STATE_TAG, "블루투스 연결 완료");
        Toast.makeText(context, "블루투스 연결 완료", Toast.LENGTH_SHORT).show();
    }

    void disconnectionCompleted() {
        isConnected = false;
        ((MainActivity) context).settingFragment.progressBar.setVisibility(View.GONE);
        ((MainActivity) context).settingFragment.conBtSwitch.setVisibility(View.VISIBLE);
        Log.d(MainActivity.STATE_TAG, "블루투스 연결 해제");
    }

    // 엑추에이터에 전송
    void writeBLT1(String msg) {
        connectedThreads[0].write(msg.getBytes());
    }

    // 침대 센서에 전송
    void writeBLT2(String msg) {
        connectedThreads[1].write(msg.getBytes());
    }

    // 밴드에 전송
    void writeBLT3(String msg) {
        connectedThreads[2].write(msg.getBytes());
    }

    // 기기 연결 후 사용
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            bltSockets.add(mmSocket);

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(MainActivity.STATE_TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(MainActivity.STATE_TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            deviceCount++;
            Log.d(MainActivity.STATE_TAG, "연결된 디바이스 수 -> " + deviceCount);
            if (deviceCount == NUM_OF_DEVICES) { // 3개의 기기 연결 완료
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        connectionCompleted();
                    }
                });
        }

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    numBytes = mmInStream.available();
                    if (numBytes != 0) {
                        SystemClock.sleep(300);
                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);
                        // Send the obtained bytes to the UI activity.
                        Message readMsg = handler.obtainMessage(
                                MessageConstants.MESSAGE_READ, numBytes, -1,
                                mmBuffer);
                        readMsg.sendToTarget();
                    }
                } catch (IOException e) {
                    Log.d(MainActivity.STATE_TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(MainActivity.STATE_TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(MainActivity.STATE_TAG, "Could not close the connect socket", e);
            }
        }
    }

    // 기기 연결에 사용
    class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(MainActivity.STATE_TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(MainActivity.STATE_TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            connected(mmSocket, mmDevice);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(MainActivity.STATE_TAG, "Could not close the client socket", e);
            }
        }
    }
}