package codepig.ffmpegcldemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private Button cameraBtn,imgBtn,musicBtn,makeBtn,switchCameraBtn;
    private ImageView imgPreview;
    private LinearLayout bufferIcon;
    private SurfaceView surfaceView;
    private MediaPlayer mPlayer;
    private MediaPlayer aPlayer;
    private Bitmap imageBitmap;
    private Handler mHandler;
    private SurfaceHolder sfHolder;
    private Uri imageUri=null;
    private Uri audioUri=null;
    private Uri videoUri=null;
    private String videoUrl="";//源视频文件(本例中是产生的录像文件)
    private String imageUrl="";//水印图文件
    private String musicUrl="";//音乐文件
    private String outputUrl="";//输出视频文件
    private int file_type=0;
    private boolean isRecording = false;
    private String recordFilename="testVideo";
    private String outputFilename="outputVideo";
    private Camera camera;
    // 录制的视频文件
    private File videoFile ;
    private MediaRecorder mRecorder;
    private boolean hasCamera=false;
    private int camIdx=Camera.CameraInfo.CAMERA_FACING_FRONT;

    private final int IMAGE_FILE=1;
    private final int MUSIC_FILE=2;
    private final int VIDEO_FILE=3;
    private final int screenW=1280;
    private final int screenH=720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_main);
        findView();
        initSurfaceView();
    }

    private void findView(){
        cameraBtn=(Button) findViewById(R.id.cameraBtn);
        switchCameraBtn=(Button) findViewById(R.id.switchCameraBtn);
        imgBtn=(Button) findViewById(R.id.imgBtn);
        musicBtn=(Button) findViewById(R.id.musicBtn);
        makeBtn=(Button) findViewById(R.id.makeBtn);
        imgPreview=(ImageView) findViewById(R.id.imgPreview);
        bufferIcon=(LinearLayout) findViewById(R.id.bufferIcon);

        bufferIcon.setVisibility(View.GONE);
        imgBtn.setOnClickListener(clickBtn);
        musicBtn.setOnClickListener(clickBtn);
        makeBtn.setOnClickListener(clickBtn);
        switchCameraBtn.setOnClickListener(clickBtn);
        cameraBtn.setOnClickListener(clickBtn);

        //初始化播放器
        mPlayer=new MediaPlayer();
        mHandler = new Handler() {  //初始化handler
            @Override
            public void handleMessage(Message msg) { //通过handleMessage()来处理传来的消息
                switch (msg.what){
                    case 0:
                        imgPreview.setImageBitmap(imageBitmap);
                        break;
                    case 1:
                        bufferIcon.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        bufferIcon.setVisibility(View.GONE);
                        makeBtn.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        };

        outputUrl=FileUtil.getPath() + "/"+outputFilename+".mp4";
        hasCamera=checkCameraHardware(context);

        //隐藏系统导航栏(android4.1及以上)
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 初始化surfaceView
     */
    private void initSurfaceView(){
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        sfHolder=surfaceView.getHolder();
        // 设置分辨率
        sfHolder.setFixedSize(screenW, screenH);
        sfHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("LOGCAT", "surfaceDestroyed");
                surfaceView=null;
                sfHolder=null;
                if(mRecorder!=null) {
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                }
            }

            //必须监听surfaceView的创建，创建完毕后才可以处理播放
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("LOGCAT", "surfaceCreated");
                if(hasCamera) {
                    openCamera();//接收到Surface的回调后启用摄像头。
                }else{
                    Toast.makeText(context, "没有摄像头，退散吧！", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("LOGCAT", "surfaceChanged");
            }
        });
    }

    /**
     * 检测摄像头
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 初始化摄像头
     */
    private void openCamera() {
        if(camera!=null){
            camera.stopPreview();
            camera.release();
            camera=null;
        }
        try {
            camera = Camera.open(camIdx);
            Parameters cP=camera.getParameters();
            cP.setPreviewSize(screenW,screenH);
            camera.setParameters(cP);
            camera.setPreviewDisplay(sfHolder);//通过SurfaceView显示取景画面
            camera.startPreview(); //开始预览
        } catch (IOException e) {
            Log.d("LOGCAT", "IOException:"+e.toString());
        }
    }

    /**
     * 按钮监听
     */
    private View.OnClickListener clickBtn = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //播放器区域按钮
                case R.id.switchCameraBtn:
                    if(camIdx==Camera.CameraInfo.CAMERA_FACING_FRONT){
                        camIdx=Camera.CameraInfo.CAMERA_FACING_BACK;
                    }else{
                        camIdx=Camera.CameraInfo.CAMERA_FACING_FRONT;
                    }
                    openCamera();
                    break;
                case R.id.cameraBtn:
                    //录制
//                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
                    isRecording = !isRecording;
                    if(audioUri!=null) {
                        playMusic(musicUrl);
                    }
                    if(isRecording)
                    {
                        startRecode();
                    }else{
                        stopRecode();
                    }
                    break;
                case R.id.imgBtn:
                    file_type=IMAGE_FILE;
                    chooseFile();
                    break;
                case R.id.musicBtn:
                    file_type=MUSIC_FILE;
                    chooseFile();
                    break;
                case R.id.makeBtn:
                    //开始合并
                    makeVideo();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 开始录制
     */
    private void startRecode(){
        try {
            Log.i("LOGCAT", "Start recording...");
            cameraBtn.setText("正在录制");
            switchCameraBtn.setVisibility(View.GONE);
            // 创建保存录制视频的视频文件
            videoFile = new File(FileUtil.getPath() + "/"+recordFilename+".mp4");
            videoUrl=FileUtil.getPath() + "/"+recordFilename+".mp4";
            camera.unlock();
            // 设置该组件让屏幕不会自动关闭
            sfHolder.setKeepScreenOn(true);
            mRecorder = new MediaRecorder();
            mRecorder.reset();
            mRecorder.setCamera(camera);
            // 设置从麦克风采集声音(麦克风声音MIC,录像机的声音AudioSource.CAMCORDER,系统声音REMOTE_SUBMIX)
//            mRecorder.setAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX);
            // 设置从摄像头采集图像
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //录制角度
//            mRecorder.setOrientationHint(90);
            // 设置视频文件的输出格式
            // 必须在设置声音编码格式、图像编码格式之前设置
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置声音编码的格式
//            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            // 设置图像编码的格式
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecorder.setVideoEncodingBitRate(5*screenW*screenH);
            mRecorder.setVideoSize(screenW, screenH);
            // 每秒24帧
            mRecorder.setVideoFrameRate(24);
            mRecorder.setOutputFile(videoFile.getAbsolutePath());
            // 指定使用SurfaceView来预览视频
            mRecorder.setPreviewDisplay(sfHolder.getSurface());
            mRecorder.prepare();
            // 开始录制
            mRecorder.start();
        }catch (IOException e){
        }
    }

    /**
     * 停止录制
     */
    private void stopRecode(){
        switchCameraBtn.setVisibility(View.VISIBLE);
        stopPlayer();
        // 设置该组件让屏幕不会自动关闭
        surfaceView.getHolder().setKeepScreenOn(false);
        Log.i("LOGCAT", "录制完毕， 存储为 " + videoFile.getPath());
        cameraBtn.setText("录制完毕");
        // 停止录制
        mRecorder.stop();
        // 释放资源
        mRecorder.release();
        mRecorder = null;
        makeBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 打开文件
     */
    private void chooseFile(){
        Intent intent = new Intent();
        //使用ACTION_PICK时google原生5.1系统音频选择会报错，使用ACTION_GET_CONTENT时小米系统获得的是空指针
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.d("LOGCAT", "file type:" + file_type);
        switch (file_type) {
            case 1:
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                break;
            case 2:
                intent.setData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                intent.setType("audio/*");
                break;
            case 3:
                intent.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                break;
        }
        startActivityForResult(intent, 0x1);
    }

    /**
     * 监听文件选择
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x1 && resultCode == Activity.RESULT_OK && data!=null) {
            switch (file_type){
                case IMAGE_FILE://不同机型系统，得到的fileUri.getPath()值不同，所以以不同的方式获取地址
                    try{
                        imageUri = data.getData();
                        Log.d("LOGCAT", "uri path:"+imageUri.getPath()+"   "+imageUri.toString());
                        String[] pojo = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(imageUri, pojo, null, null, null);
                        if (cursor != null) {
                            /*这部分代码在ACTION_GET_CONTENT模式下为空，在ACTION_PICK模式下可以得到具体地址
                            int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            imageUrl = cursor.getString(colunm_index);
                            cursor.close();
                            //以上代码获取图片路径
                            Log.d("LOGCAT","path:"+imageUrl);
                            */
                            imgPreview.setImageURI(imageUri);
                        }else{
                            imageUrl=imageUri.getPath();
                            Log.d("LOGCAT","path:"+imageUrl);
                            Runnable bmpR=new Runnable() {
                                @Override
                                public void run() {
                                    imageBitmap = imageLoader.returnBitMapLocal(imageUrl, 300, 200);
                                    if (imageBitmap != null){
                                        Message msg = new Message();
                                        msg.what = 0;
                                        mHandler.sendMessage(msg);
                                    }
                                }
                            };
                            ThreadPoolUtils.execute(bmpR);
                        }
                    }catch (Exception e){
                        Log.d("LOGCAT",e.toString());
                    }
                    break;
                case MUSIC_FILE:
                    try{
                        audioUri = data.getData();
                        Log.d("LOGCAT", "uri path:"+audioUri.getPath()+"   "+audioUri.toString());
                        String[] pojo = {MediaStore.Audio.Media.DATA};
                        Cursor cursor = getContentResolver().query(audioUri, pojo, null, null, null);
                        if (cursor != null) {
                            /*这部分代码在ACTION_GET_CONTENT模式下为空，在ACTION_PICK模式下可以得到具体地址
                            int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            imageUrl = cursor.getString(colunm_index);
                            cursor.close();
                            */
                        }else{
                            musicUrl=audioUri.getPath();
                            Log.d("LOGCAT","path:"+musicUrl);
                        }
                    }catch (Exception e){
                        Log.d("LOGCAT",e.toString());
                    }
                    break;
                case VIDEO_FILE:
                    try{
                        videoUri = data.getData();
                        Log.d("LOGCAT", "uri path:"+videoUri.getPath()+"   "+videoUri.toString());
                        String[] pojo = {MediaStore.Video.Media.DATA};
                        Cursor cursor = getContentResolver().query(videoUri, pojo, null, null, null);
                        if (cursor != null) {
                            /*这部分代码在ACTION_GET_CONTENT模式下为空，在ACTION_PICK模式下可以得到具体地址
                            int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            imageUrl = cursor.getString(colunm_index);
                            cursor.close();
                            */
                        }else{
                            videoUrl=videoUri.getPath();
                            Log.d("LOGCAT","path:"+videoUrl);
                            playVideo(videoUrl);
                        }
                    }catch (Exception e){
                        Log.d("LOGCAT",e.toString());
                    }
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 播放视频
     * @param _url
     */
    public void playVideo(String _url){
        if(_url!=""){
            try {
                Log.d("LOGCAT", "play:" + _url);
                if(mPlayer==null){
                    Log.d("LOGCAT", "new player");
                    mPlayer=new MediaPlayer();
                }else{
                    Log.d("LOGCAT", "reset player");
                    mPlayer.reset();
                }
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                //设置需要播放的视频
                mPlayer.setDataSource(_url);
                mPlayer.prepareAsync();
                mPlayer.setOnBufferingUpdateListener(bufferingListener);
                mPlayer.setOnPreparedListener(preparedListener);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /**
     * 播放音乐
     */
    private void playMusic(String _url){
        if(aPlayer==null){
            try {
                aPlayer = new MediaPlayer();
                aPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                aPlayer.setDataSource(_url);
                aPlayer.prepareAsync();
                Log.d("LOGCAT","set audioPlayer");
                aPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // 装载完毕 开始播放流媒体
                        Log.d("LOGCAT","play audioPlayer");
                        aPlayer.start();
                    }
                });
            }catch (Exception e){
                Log.d("LOGCAT","err:"+e.toString());
            }
        }else{
            Log.d("LOGCAT","has audioPlayer");
            if(!aPlayer.isPlaying()) {
                Log.d("LOGCAT","reStart");
                aPlayer.start();
            }
        }
    }

    /**
     * 停止播放
     */
    private void stopPlayer(){
        if(mPlayer!=null && mPlayer.isPlaying()){
            mPlayer.stop();
        }
        if(aPlayer!=null && aPlayer.isPlaying()){
            aPlayer.stop();
        }
    }

    /**
     * 监听缓冲进度更新
     */
    MediaPlayer.OnBufferingUpdateListener bufferingListener=new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
        }
    };

    /**
     * prepare监听
     */
    MediaPlayer.OnPreparedListener preparedListener=new MediaPlayer.OnPreparedListener(){
        @Override
        public void onPrepared(MediaPlayer mp)
        {
            bufferIcon.setVisibility(View.GONE);
            //播放
            mPlayer.start();
        }
    };

    /**
     * 添加图片水印及音乐
     */
    private void makeVideo(){
        if(imageUri==null){
            Toast.makeText(this, "少年，不先选张图片么？", Toast.LENGTH_SHORT).show();
            return;
        }
        if(audioUri==null){
            Toast.makeText(this, "少年，不先选首音乐么？", Toast.LENGTH_SHORT).show();
            return;
        }
        Runnable compoundRun=new Runnable() {
            @Override
            public void run() {
                String[] commands = new String[11];
                commands[0] = "ffmpeg";
                //输入
                commands[1] = "-i";
                commands[2] = videoUrl;
                //水印
                commands[3] = "-i";
                commands[4] = imageUrl;
                commands[5] = "-filter_complex";
                commands[6] = "overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2";
                //音乐
                commands[7] = "-i";
                commands[8] = musicUrl;
                //覆盖输出
                commands[9] = "-y";
                //输出文件
                commands[10] = outputUrl;

//                commands[10] = "-strict";//标准的严格性
//                commands[11] = "-2";
//                commands[12] = "-y";//直接覆盖输出文件

                FFmpegKit.execute(commands, new FFmpegKit.KitInterface() {
                    @Override
                    public void onStart() {
                        Log.d("FFmpegLog LOGCAT","FFmpeg 命令行开始执行了...");
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onProgress(int progress) {
                        Log.d("FFmpegLog LOGCAT","done com"+"FFmpeg 命令行执行进度..."+progress);
                    }

                    @Override
                    public void onEnd(int result) {
                        Log.d("FFmpegLog LOGCAT","FFmpeg 命令行执行完成...");
                        Message msg = new Message();
                        msg.what = 2;
                        mHandler.sendMessage(msg);
                    }
                });
            }
        };
        ThreadPoolUtils.execute(compoundRun);
    }

    @Override
    public void onPause() {
        Log.d("LOGCAT", "player onPause");
        camera.release();
        camera=null;
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d("LOGCAT", "player onResume");
        initSurfaceView();
        openCamera();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d("LOGCAT", "player onDestroy");
        super.onDestroy();
    }
}
