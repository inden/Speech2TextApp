package com.in.den.android.speech2textapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class StandaloneActivity extends AppCompatActivity
        implements TextToSpeech.OnInitListener {

    private SpeechRecognizer sr;
    private TextToSpeech textToSpeech;

    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 0;
    TextView textView;
    private Button startButton;
    private String TAG = "StandaloneActivity";
    private boolean bLocalLanguageAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userPermission();

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        startButton = (Button) findViewById(R.id.startbutton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                startListening();
            }
        });

        initText2Speak();
    }

    private void userPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

            }
        }
    }

    private void initText2Speak() {

        textToSpeech = new TextToSpeech(this, this);

    }

    private void speakOut() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UID");
        //Lollipop
        textToSpeech.speak(textView.getText().toString(), TextToSpeech.QUEUE_FLUSH, map);

        //It is important set this listner after speak method called otherwise error
        textToSpeech.setOnUtteranceProgressListener(progressListner);

        /*
        this require the permission
        String exStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("MainActivity", "exStoragePath : "+exStoragePath);
        File appTmpPath = new File(exStoragePath + "/myappsounds/");
        boolean isDirectoryCreated = appTmpPath.mkdirs();
        Log.d("MainActivity", "directory "+appTmpPath+" is created : "+isDirectoryCreated);
        String tempFilename = "tmpaudio.wav";
        String tempDestFile = appTmpPath.getAbsolutePath() + File.separator + tempFilename;

        textToSpeech.synthesizeToFile(textView.getText().toString(),map,  tempDestFile);
        */
    }

    private final UtteranceProgressListener progressListner =
            new UtteranceProgressListener() {
        @Override
        public void onDone (String utteranceId){
            Log.d(TAG, "speech finished");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startButton.setEnabled(true);
                    startListening();
                }
            });
        }

        @Override
        public void onError (String utteranceId){
        }

        @Override
        public void onStart (String utteranceId){
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // startListening();
    }

    @Override
    protected void onPause() {
        stopListening();
        stopSpeaking();
        super.onPause();
    }

    private void stopSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    protected void startListening() {
        try {
            if (sr == null) {
                sr = SpeechRecognizer.createSpeechRecognizer(this);
                if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "SpeechRecognition is not available",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                sr.setRecognitionListener(new listener());
            }

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            sr.startListening(intent);

            //UI Change
            startButton.setEnabled(false);


        } catch (Exception ex) {
            /*
            Toast.makeText(getApplicationContext(), "error happend in startListening()",
                    Toast.LENGTH_LONG).show();*/
            Log.e(TAG, ex.getMessage());
            finish();
        }
    }

    protected void stopListening() {
        if (sr != null) sr.destroy();
        sr = null;
    }

    public void restartListeningService() {
        stopListening();
        startListening();
    }

    @Override
    public void onInit(int status) {
        if (textToSpeech.isLanguageAvailable(Locale.getDefault()) !=
                TextToSpeech.LANG_NOT_SUPPORTED) {
            textToSpeech.setLanguage(Locale.getDefault());
            bLocalLanguageAvailable = true;
        } else {
            textToSpeech.setLanguage(Locale.US);
            bLocalLanguageAvailable = false;

        }
    }

    class listener extends MyRecognitionListener {

        public void onError(int error) {
            String reason = getErrorReason(error);

            Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
            restartListeningService();
        }

        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), R.string.listening,
                    Toast.LENGTH_LONG).show();
        }

        public void onResults(Bundle results) {

            Log.d(TAG, "**onResults**");
            ArrayList results_array = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);

            String resultsString;
            /*
            for (int i = 0; i < results.size(); i++) {
                resultsString += results_array.get(i) + ";";
            }*/

            resultsString = (String) results_array.get(0);

            //Toast.makeText(getApplicationContext(), resultsString, Toast.LENGTH_LONG).show();

            processResultat(resultsString);
        }

        private void processResultat(String s) {

            s = s.trim();
            s = s.toUpperCase();

            int commandenum = isCommand(s);

            if (commandenum == 0) {
                textView.setText(textView.getText() + "\r\n" + s);
                restartListeningService();
            } else if (commandenum == 1) {
                stopListening();
                speakOut();

                //startlistning is called at the end of the speech

            } else {
                stopListening();

                //UIChange
                startButton.setEnabled(true);
            }
        }


        private int isCommand(String s) {
            int commande = 0;

            if ("OK GOOGLE".equals(s)) commande = 1;
            else if ("BYE BYE".equals(s)) commande = 2;

            return commande;
        }


    }
}
