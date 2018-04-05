package com.mobven.recordandplay;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.EnvironmentalReverb;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    boolean isRecording = false;
    private Button btnRecord;
    private Button btnPlay;
    private Button btnEffect;
    private MediaPlayer mediaPlayer = null;
    private MediaRecorder mRecorder = null;
    private EnvironmentalReverb environmentalReverb = null;
    private int audioSessionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Paper.init(this);
        btnRecord = findViewById(R.id.record);
        btnPlay = findViewById(R.id.play);
        btnEffect = findViewById(R.id.effect);

        btnRecord.setBackgroundColor(Color.GRAY);
        btnEffect.setBackgroundColor(Color.GRAY);
        btnPlay.setBackgroundColor(Color.GRAY);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordStopAction();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMedia();
            }
        });

        btnEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOrRemoveEffects( -1000, -500, 3920,  700, -1230,  20,    -2,  29, 1000,1000  );
            }
        });

    }

    private void setOrRemoveEffects(int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8, int v9, int v10){
        if(environmentalReverb == null || audioSessionId == 0) {
            try {
                if (mediaPlayer != null)
                    audioSessionId = mediaPlayer.getAudioSessionId();
                else
                    audioSessionId = 0;
                environmentalReverb = new EnvironmentalReverb(1, audioSessionId);
                environmentalReverb.setRoomLevel((short) v1);
                environmentalReverb.setRoomHFLevel((short) v2);
                environmentalReverb.setDecayTime(v3);
                environmentalReverb.setDecayHFRatio((short) v4);
                environmentalReverb.setReflectionsLevel((short) v5);
                environmentalReverb.setReflectionsDelay(v6);
                environmentalReverb.setReverbLevel((short) v7);
                environmentalReverb.setReverbDelay(v8);
                environmentalReverb.setDiffusion((short) v9);
                environmentalReverb.setDensity((short) v10);
                environmentalReverb.setEnabled(true);
                btnEffect.setBackgroundColor(Color.GREEN);
            } catch (RuntimeException e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            }
        } else if (environmentalReverb.getEnabled()) {
            environmentalReverb.setEnabled(false);
            btnEffect.setBackgroundColor(Color.GRAY);
        }
        else {
            environmentalReverb.setEnabled(true);
            btnEffect.setBackgroundColor(Color.GREEN);
        }
    }

    private void startMedia(){
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(Paper.book().read("recordPath", ""));
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                         mediaPlayer.start();
                        btnPlay.setBackgroundColor(Color.GREEN);
                    }
                });
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                    }
                });

                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        return stopMediaPlayer();
                    }
                });
            } catch (Exception e) {
                e.getStackTrace();
            }
        } else if(mediaPlayer.isPlaying()) {
            stopMediaPlayer();
        }
    }

    private void recordStopAction(){
        if(mediaPlayer != null && mediaPlayer.isPlaying())
            stopMediaPlayer();
        if(isRecording){
            stopRecording();
            btnRecord.setBackgroundColor(Color.GRAY);
            isRecording = false;
        } else {
            startRecording();
            btnRecord.setBackgroundColor(Color.RED);
            isRecording = true;
        }
    }

    public void startRecording() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(getFilename());
            isRecording = true;
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
        }
    }

    public void stopRecording() {

        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private boolean stopMediaPlayer(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            btnPlay.setBackgroundColor(Color.GRAY);
            return true;
        }else
            return false;
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath);

        if (!file.exists()) {
            file.mkdirs();
        }
        String voiceRecordPath = file.getAbsolutePath() + "/RecordAndPlay" + ".wav";
        Log.e("recordpath", voiceRecordPath);
        Paper.book().write("recordPath", voiceRecordPath);
        return (voiceRecordPath);
    }
}
