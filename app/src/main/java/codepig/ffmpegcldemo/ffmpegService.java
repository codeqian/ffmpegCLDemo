package codepig.ffmpegcldemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


/**
 * 以service形式调用ffmpeg
 * Created by qzd on 2017/7/13.
 */

public class ffmpegService extends Service {
    private final String TAG = "MyService";
    private int startId;
    public enum Control {
        DOFFMPEG, GETPEC
    }

    public ffmpegService() {
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand---startId: " + startId);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Control control = (Control) bundle.getSerializable("Key");
            if (control != null) {
                switch (control) {
                    case DOFFMPEG:
                        break;
                    case GETPEC:
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class mBinder extends Binder {
        public void startFFmpeg() {
            Log.d("TAG", "startFFmpeg() executed");
            // 执行具体的下载任务
        }
    }
}
