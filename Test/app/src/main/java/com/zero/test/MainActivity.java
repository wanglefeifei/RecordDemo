package com.zero.test;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AudioManager mAudioManager;
    private MediaRecorder recorder;
    private Button openBluetoothMIC_bt, record_bt, stop_bt, play_bt,openPhoneMIC_bt;
    private String path;
    private Toast toast;
    private TextView useMic_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        openBluetoothMIC_bt = (Button) findViewById(R.id.openBluetoothMIC_bt);
        record_bt = (Button) findViewById(R.id.record_bt);
        stop_bt = (Button) findViewById(R.id.stop_bt);
        play_bt = (Button) findViewById(R.id.play_bt);
        openPhoneMIC_bt = (Button) findViewById(R.id.openPhoneMIC_bt);
        useMic_tv = (TextView) findViewById(R.id.useMic_tv);
        openBluetoothMIC_bt.setOnClickListener(this);
        record_bt.setOnClickListener(this);
        stop_bt.setOnClickListener(this);
        play_bt.setOnClickListener(this);
        openPhoneMIC_bt.setOnClickListener(this);
        boolean sdExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdExist) {
            if (getExternalFilesDir(null) != null) {
                path = getExternalFilesDir(null).getAbsolutePath()  + "/sound.amr";  //设置音频文件保存路径
            } else {
                path = getFilesDir().getAbsolutePath() + "/sound.amr";
            }
        } else {
            path = getFilesDir().getAbsolutePath() + "/sound.amr";
        }
        ((TextView)findViewById(R.id.tips_tv)).setText("音频存储路径："+path);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.openBluetoothMIC_bt:
                mAudioManager.startBluetoothSco();
                mAudioManager.setSpeakerphoneOn(false);
                mAudioManager.setBluetoothScoOn(true);
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

//                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
//                mAudioManager.setBluetoothScoOn(true);
//                mAudioManager.startBluetoothSco();
                useMic_tv.setText("蓝牙麦克风");
                break;
            case R.id.openPhoneMIC_bt:
                if(mAudioManager.isBluetoothScoOn()){
                    mAudioManager.stopBluetoothSco();
                }
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                mAudioManager.setSpeakerphoneOn(true);
                mAudioManager.setBluetoothScoOn(false);
                useMic_tv.setText("本机麦克风");
                break;
            case R.id.record_bt:
                try {
                    recorder = new MediaRecorder();
//                    recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                    //
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                    recorder.setOutputFile(path);

                    // 设置录制的声音的输出格式（必须在设置声音编码格式之前设置）
                    recorder.setOutputFormat(MediaRecorder
                            .OutputFormat.AMR_NB);
                    // 设置声音编码的格式
                    recorder.setAudioEncoder(MediaRecorder
                            .AudioEncoder.AMR_NB);
                    recorder.prepare();
                    recorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                record_bt.setVisibility(View.GONE);
                stop_bt.setVisibility(View.VISIBLE);
                showToast("开始录音");
                break;
            case R.id.stop_bt:
                recorder.stop();
                recorder.release();
                recorder = null;
                stop_bt.setVisibility(View.GONE);
                record_bt.setVisibility(View.VISIBLE);
                showToast("停止录音");
                break;
            case R.id.play_bt:
                File file = new File(path);
                if (!file.exists()){
                    showToast("没有录音文件");
                }
                Uri uri = Uri.parse(path);
                final int mode = mAudioManager.getMode();
                final boolean ifBlue = mAudioManager.isBluetoothScoOn();
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                if(ifBlue){
                    mAudioManager.stopBluetoothSco();
//                    mAudioManager.setBluetoothScoOn(false);
//                    mAudioManager.setSpeakerphoneOn(true);
                }
                MediaPlayer player =MediaPlayer.create(this,uri);
                player.start();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mAudioManager.setMode(mode);
                        if(ifBlue){
                            mAudioManager.startBluetoothSco();
                            Log.i("ssss","startSco");
                        }
                    }
                });
                showToast("开始播放");
                break;
        }
    }

    @Override
    protected void onPause() {
        openPhoneMIC_bt.performClick();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    private void showToast(String s){
        if (toast!=null){
            toast.setText(s);
        }else {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}
