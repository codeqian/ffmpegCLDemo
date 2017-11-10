package codepig.ffmpegcldemo;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import codepig.ffmpegcldemo.ffmpegCentre.ffmpegCommandCentre;
import codepig.ffmpegcldemo.utils.FileUtil;
import codepig.ffmpegcldemo.utils.ThreadPoolUtils;


/**
 * 以service形式调用ffmpeg
 * Created by qzd on 2017/7/13.
 */

public class ffmpegService extends IntentService {
    private final String TAG = "MyService";
    private Handler handler = new Handler();
    private final int ENCODEING=8;
    private final int ENCODED=9;

    public ffmpegService() {
        super("ffmpegService");
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
    }

    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        String outputUrl = bundle.getString("outputUrl");

        String[] commands= ffmpegCommandCentre.concatVideo(FileUtil.getPath()+"/list4concat.txt",outputUrl);
        final String[] _commands=commands;

        Runnable compoundRun=new Runnable() {
            @Override
            public void run() {
                FFmpegKit.execute(_commands, new FFmpegKit.KitInterface() {
                    @Override
                    public void onStart() {
                        Log.d("FFmpegLog LOGCAT","FFmpeg 命令行开始执行了...");
                        Message msg = new Message();
                        msg.what = ENCODEING;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onProgress(int progress) {
                        Log.d("FFmpegLog LOGCAT","done com"+"FFmpeg 命令行执行进度..."+progress);
                    }

                    @Override
                    public void onEnd(int result) {
                        Log.d("FFmpegLog LOGCAT","FFmpeg 命令行执行完成...");
                        Message msg = new Message();
                        msg.what = ENCODED;
                        handler.sendMessage(msg);
                    }
                });
            }
        };
        ThreadPoolUtils.execute(compoundRun);
    }
//
    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        handler.removeCallbacks(run);
        super.onDestroy();
    }

    /**
     * 用来更新进度
     */
    private Runnable run = new Runnable() {
        public void run() {
        }
    };
}
