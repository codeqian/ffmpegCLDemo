package codepig.ffmpegcldemo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class splashActivity extends AppCompatActivity {
    private int skipCount=3;
    private Handler splashHandler;
    private Runnable enterRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashHandler=new Handler();
        enterRunnable=new Runnable() {
            @Override
            public void run() {
                skipCount--;
                if (skipCount == 0) {
                    startActivity(new Intent(getApplication(),MainActivity.class));
                    finish();
                }else{
                    splashHandler.postDelayed(enterRunnable, 1000);
                }
            }
        };
        splashHandler.postDelayed(enterRunnable, 1000);
    }
}
