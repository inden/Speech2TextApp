package com.in.den.android.speech2textapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SpeechRecogService extends Service {

    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler());

    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static final int MSG_RECONGNIZER_REPLYTO = 3;
    static final int MSG_REPLY_RESULT = 4;
    static final int MSG_REPLY_ERROR = 5;
    static final int MSG_REPLY_READY = 6;


    String TAG = SpeechRecogService.class.getSimpleName();

    private SpeechRecognitionListener speechRecognitionListener = new SpeechRecognitionListener();

    private Messenger replytoMessenger = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(speechRecognitionListener);

        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
    }
/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }*/

    protected void init() {
        if(mSpeechRecognizer != null) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(speechRecognitionListener);
        }

    }

    protected class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            init();

            switch (msg.what) {
                case MSG_RECONGNIZER_REPLYTO:
                    if (msg.replyTo != null) {
                        replytoMessenger = msg.replyTo;
                    }
                    break;

                case MSG_RECOGNIZER_START_LISTENING:
                    if (!mIsListening) {
                       mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        mIsListening = true;
                        //Log.d(TAG, "message start listening"); //$NON-NLS-1$

                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:

                    mSpeechRecognizer.cancel();
                    mIsListening = false;
                    //Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServerMessenger.getBinder();
    }

    protected class SpeechRecognitionListener extends MyRecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "***onReadyForSpeech***");

            replyMessege(MSG_REPLY_READY, "");

        }

        @Override
        public void onError(int error) {
            destroy();

            String reason = getErrorReason(error);
            replyMessege(MSG_REPLY_ERROR, reason);
        }

        @Override
        public void onResults(Bundle results) {
            //Log.d(TAG, "onResults");
            destroy();

            ArrayList results_array = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            // 取得した文字列を結合
            String resultsString = "";
            for (int i = 0; i < results.size(); i++) {
                resultsString += results_array.get(i) + ";";
            }

            replyMessege(MSG_REPLY_RESULT, resultsString);
        }

        private void destroy() {
            mIsListening = false;
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
        }

        private void replyMessege(int what, Object o) {
            if (replytoMessenger != null) {
                Message rmsg = new Message();
                rmsg.obj = o;
                rmsg.what = what;

                try {
                    replytoMessenger.send(rmsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
