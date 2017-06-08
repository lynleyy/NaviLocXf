package com.sannas.navilocxf.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.sannas.navilocxf.R;
import com.sannas.navilocxf.util.SpeechUtils;


public class Main2Activity extends AppCompatActivity {

    private static final String TAG = "Main2Activity";
    EditText et;
    Button btn;
    private SpeechUtils speechUtils;
    private SpeechSynthesizer mTts;
    String text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        et = (EditText) findViewById(R.id.et);
        btn = (Button) findViewById(R.id.btn);
//        speechUtils = new SpeechUtils(this);

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=58216007");
        //语音合成
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        //保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
        //如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        //合成监听器

//
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                speechUtils.startSpeaking(et.getText().toString());
                //  1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
//                speechUtils.startListening();
                text = et.getText().toString();
                mTts.startSpeaking(text, listener);
            }
        });
    }

    SynthesizerListener listener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            if (speechError == null) {
                mTts.startSpeaking(et.getText().toString(), listener);
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };


}
