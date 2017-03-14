package com.in.den.android.speech2textapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class UseServiceActivity extends AppCompatActivity {

    private int mBindFlag;
    private Messenger mServiceMessenger;
    protected final Messenger mReceieveMessenger = new Messenger(new RecievegHandler());
    private Intent intentservice;
    private boolean bounded = false;
    private static String TAG = UseServiceActivity.class.getSimpleName();
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;

        intentservice = new Intent(this, SpeechRecogService.class);
        startService(intentservice);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "bindservice");
        bindService(intentservice, mServiceConnection, mBindFlag);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mServiceMessenger != null) {
            unbindService(mServiceConnection);
            mServiceMessenger = null;
        }
        stopService(intentservice);
    }

    private void makeRequest2Service(int what) {
        if(!bounded) return;
        Message msg = new Message();
        msg.what = what;
        msg.replyTo = mReceieveMessenger;

        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mServiceMessenger = new Messenger(service);
            bounded = true;
            makeRequest2Service(SpeechRecogService.MSG_RECONGNIZER_REPLYTO);

            makeRequest2Service(SpeechRecogService.MSG_RECOGNIZER_START_LISTENING);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mServiceMessenger = null;
            bounded = false;
        }

    }; // mServiceConnection

    private class RecievegHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, msg.obj.toString());

            switch(msg.what) {
                case SpeechRecogService.MSG_REPLY_ERROR :
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                case SpeechRecogService.MSG_REPLY_READY:
                    Toast.makeText(getApplicationContext(), "please speak", Toast.LENGTH_LONG).show();
                    break;
                case SpeechRecogService.MSG_REPLY_RESULT:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
            }

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    makeRequest2Service(SpeechRecogService.MSG_RECOGNIZER_START_LISTENING);
                }
            }, 0, 2000);

        }

    }
}
