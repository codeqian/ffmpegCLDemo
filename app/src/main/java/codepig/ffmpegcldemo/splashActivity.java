package codepig.ffmpegcldemo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class splashActivity extends AppCompatActivity {
    private int skipCount=2;
    private Handler splashHandler;
    private Runnable enterRunnable;
//    private TextView title_t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        title_t=(TextView) findViewById(R.id.title_t);
//        title_t.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getApplication(),MainActivity.class));
//            }
//        });
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
