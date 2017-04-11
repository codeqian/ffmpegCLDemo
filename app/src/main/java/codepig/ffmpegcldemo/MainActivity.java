package codepig.ffmpegcldemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button cameraBtn,imgBtn,musicBtn,videoBtn,switchCameraBtn;
    private ImageView imgPreview;
    private ProgressBar bufferIcon;
    private SurfaceView surfaceView;
    private MediaPlayer mPlayer;
    private MediaPlayer aPlayer;
    private Bitmap imageBitmap;
    private Handler mHandler;
    private SurfaceHolder sfHolder;
    private Uri imageUri=null;
    private Uri audioUri=null;
    private Uri videoUri=null;
    private String videoUrl="";
    private String imageUrl="";
    private String musicUrl="/storage/sdcard0/boosjdance/media/14113.mp4";
    private String outputUrl="";
    private String ffmpegUrl="";
    private int file_type=0;
    private boolean isRecording = false;
    private String recordFilename="testVideo";
    private String outputFilename="outputVideo";
    // 系统的视频文件
    private File videoFile ;
    private MediaRecorder mRecorder;

    private final int IMAGE_FILE=1;
    private final int MUSIC_FILE=2;
    private final int VIDEO_FILE=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        initSurfaceView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void findView(){
        cameraBtn=(Button) findViewById(R.id.cameraBtn);
        switchCameraBtn=(Button) findViewById(R.id.switchCameraBtn);
        imgBtn=(Button) findViewById(R.id.imgBtn);
        musicBtn=(Button) findViewById(R.id.musicBtn);
        videoBtn=(Button) findViewById(R.id.videoBtn);
        imgPreview=(ImageView) findViewById(R.id.imgPreview);
        bufferIcon=(ProgressBar) findViewById(R.id.bufferIcon);

        bufferIcon.setVisibility(View.GONE);
        imgBtn.setOnClickListener(clickBtn);
        musicBtn.setOnClickListener(clickBtn);
        videoBtn.setOnClickListener(clickBtn);
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
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 按钮监听
     */
    private View.OnClickListener clickBtn = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //播放器区域按钮
                case R.id.cameraBtn:
                    //录制
                    isRecording = !isRecording;
                    if(audioUri!=null) {
                        playMusic(musicUrl);
                    }
                    if(isRecording)
                    {
                        try {
                            // 设置该组件让屏幕不会自动关闭
                            sfHolder.setKeepScreenOn(true);
                            cameraBtn.setText("正在录制");
                            Log.i("LOGCAT", "Start recording...");
                            // 创建保存录制视频的视频文件
//                            recordFilename = FileUtil.getPath() + "/rec_" + System.currentTimeMillis() + ".mp4";
                            videoFile = new File(FileUtil.getPath() + "/"+recordFilename+".mp4");
                            videoUrl=FileUtil.getPath() + "/"+recordFilename+".mp4";
                            // 创建MediaPlayer对象
                            mRecorder = new MediaRecorder();
                            mRecorder.reset();
                            // 设置从麦克风采集声音(或来自录像机的声音AudioSource.CAMCORDER)
//                            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            // 设置从摄像头采集图像
                            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                            // 设置视频文件的输出格式
                            // 必须在设置声音编码格式、图像编码格式之前设置
                            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                            // 设置声音编码的格式
//                            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                            // 设置图像编码的格式
                            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                            mRecorder.setVideoEncodingBitRate(5*1280*720);
                            mRecorder.setVideoSize(1280, 720);
                            // 每秒20帧
                            mRecorder.setVideoFrameRate(20);
                            mRecorder.setOutputFile(videoFile.getAbsolutePath());
                            // 指定使用SurfaceView来预览视频
                            mRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
                            mRecorder.prepare();
                            // 开始录制
                            mRecorder.start();
                        }catch (IOException e){
                        }
                    }
                    else
                    {
                        stopPlayer();
                        // 设置该组件让屏幕不会自动关闭
                        surfaceView.getHolder().setKeepScreenOn(false);
                        Log.i("LOGCAT", "录制完毕， 存储为 " + videoFile.getPath());
                        cameraBtn.setText("录制完毕");
                        Log.i("LOGCAT", "End recording...");
                        // 停止录制
                        mRecorder.stop();
                        // 释放资源
                        mRecorder.release();
                        mRecorder = null;
                        if(!musicUrl.equals("")){
                            //开始合并
                            compoundVideo();
                        }
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
                case R.id.videoBtn:
                    file_type=VIDEO_FILE;
                    chooseFile();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 初始化surfaceView
     */
    private void initSurfaceView(){
        // 获取程序界面中的SurfaceView
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        sfHolder=surfaceView.getHolder();
        // 设置分辨率
        sfHolder.setFixedSize(1280, 720);
        sfHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("LOGCAT", "surfaceDestroyed");
            }

            //必须监听surfaceView的创建，创建完毕后才可以处理播放
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("LOGCAT", "surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("LOGCAT", "surfaceChanged");
            }
        });
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
//                    mPlayer.setDataSource("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");//test
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
     * ffmpeg操作
     */
    private void compoundVideo(){
        outputUrl=FileUtil.getPath() + "/"+outputFilename+".mp4";
        Runnable compoundRun=new Runnable() {
            @Override
            public void run() {
                String[] commands = new String[9];
                commands[0] = "ffmpeg";
                commands[1] = "-i";
                commands[2] = videoUrl;
                commands[3] = "-i";
                commands[4] = musicUrl;
                commands[5] = "-strict";
                commands[6] = "-2";
                commands[7] = "-y";
                commands[8] = outputUrl;
                Log.d("LOGCAT","start com:"+commands.toString());

                FFmpegKit.execute(commands, new FFmpegKit.KitInterface() {
                    @Override
                    public void onStart() {
                        Log.d("LOGCAT","FFmpeg 命令行开始执行了...");
                    }

                    @Override
                    public void onProgress(int progress) {
                        Log.d("LOGCAT","done com"+"FFmpeg 命令行执行进度..."+progress);
                    }

                    @Override
                    public void onEnd(int result) {
                        Log.d("LOGCAT","FFmpeg 命令行执行完成...");
                    }
                });

//                int result = FFmpegKit.run(commands);
//                Log.d("LOGCAT","start run");
//                if(result == 0){
//                    Message msg = new Message();
//                    msg.what = 1;
//                    mHandler.sendMessage(msg);
//                    Log.d("LOGCAT","done com");
//                    Toast.makeText(MainActivity.this, "命令行执行完成", Toast.LENGTH_SHORT).show();
//                }
            }
        };
        ThreadPoolUtils.execute(compoundRun);
    }
}
