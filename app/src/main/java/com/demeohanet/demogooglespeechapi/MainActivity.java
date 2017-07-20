package com.demeohanet.demogooglespeechapi;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.demeohanet.demogooglespeechapi.util.SpeechRecognizer;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.speech.v1beta1.Speech;
import com.google.api.services.speech.v1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1beta1.model.RecognitionAudio;
import com.google.api.services.speech.v1beta1.model.RecognitionConfig;
import com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeRequest;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeResponse;

public class MainActivity extends Activity implements SpeechRecognizer.VoiceRecognizedListener {

    static SpeechRecognizer recognizer;
    boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recognizer = new SpeechRecognizer();
        recognizer.setVoiceRecognizedListener(this);
        try {
            recognizer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getText(final byte[] audioData) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String base64EncodedData = Base64.encodeBase64String(audioData);
                    RecognitionAudio recognitionAudio = new RecognitionAudio();
                    recognitionAudio.setContent(base64EncodedData);
                    TranscribeFile(recognitionAudio);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void TranscribeFile(RecognitionAudio recognitionAudio) {
        try {
            Speech speechService = new Speech.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(),
                    null).setSpeechRequestInitializer(
                    new SpeechRequestInitializer(""))
                    .build();
            RecognitionConfig recognitionConfig = new RecognitionConfig();
            recognitionConfig.setLanguageCode("vi-VN");
            SyncRecognizeRequest request = new SyncRecognizeRequest();
            request.setConfig(recognitionConfig);
            request.setAudio(recognitionAudio);
            SyncRecognizeResponse response = speechService.speech().syncrecognize(request).execute();
            SpeechRecognitionResult result = response.getResults().get(0);
            final String transcript = result.getAlternatives().get(0).getTranscript();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView speechToTextResult = (TextView) findViewById(R.id.txt_text);
                    speechToTextResult.setText(transcript);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognizer.stop();
        recognizer = null;
    }

    @Override
    public void onVoiceRecognized(byte[] audioData) {
        if (audioData != null) {
            Log.e("toannt", "push data");
            getText(audioData);
        }
    }
}
