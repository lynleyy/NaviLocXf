package com.sannas.navilocxf.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;


/**
 * 作者： XMZ on 2017/6/6 16:41.
 * 邮箱：Lynley1207@163.com
 */

public class SpeechUtils {
    private Context context;
    private static final String TAG = "SpeechUtils";
    private SpeechSynthesizer mTts;
    private RecognizerDialog iatDialog;

    public SpeechUtils(Context context) {
        this.context = context;
        initSpeechUtils();
    }

    private void initSpeechUtils() {
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=58216007");

        //语音合成
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(context, null);
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


        //语音识别为文字
        iatDialog = new RecognizerDialog(context, mInitListener);
        //2.设置听写参数，同上节
        //3.设置回调接口
        iatDialog.setListener(recognizerDialogListener);


    }


    public synchronized void startSpeaking(String speakText) {
        //3.开始合成
        if (mTts.isSpeaking()) {
            mTts.stopSpeaking();
            mTts.startSpeaking(speakText, mSynListener);
        } else {
            mTts.startSpeaking(speakText, mSynListener);
        }

    }
    public void stopSpeakding(){
        if(mTts.isSpeaking()){
            mTts.stopSpeaking();
        }
    }

    public void startListening() {
        iatDialog.show();
    }

    public void stopListening() {
        iatDialog.dismiss();
    }

    public boolean isSpeaking() {
        return mTts.isSpeaking();
    }


    //合成语音的监听
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {

        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
            Log.d(TAG, "onSpeakBegin: ");
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    };


    //听写的监听
    private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
//            Log.d(TAG, "onResult: " + recognizerResult.getResultString());
            if (!b) {
                String result = JsonParser.parseIatResult(recognizerResult.getResultString());
                Log.d(TAG, "onResult: " + result);
            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    };


    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.d(TAG, "onInit:初始化失败，错误码：" + code);
            }
        }
    };
}
