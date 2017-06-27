package codepig.ffmpegcldemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ant.liao.GifView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import codepig.ffmpegcldemo.config.deviceInfo;
import codepig.ffmpegcldemo.ffmpegCentre.ffmpegCommandCentre;
import codepig.ffmpegcldemo.listener.fileListener;
import codepig.ffmpegcldemo.net.imageLoader;
import codepig.ffmpegcldemo.utils.FileUtil;
import codepig.ffmpegcldemo.utils.ThreadPoolUtils;
import codepig.ffmpegcldemo.utils.bitmapFactory;
import codepig.ffmpegcldemo.utils.mathFactory;
import codepig.ffmpegcldemo.utils.videoUtils;
import codepig.ffmpegcldemo.values.videoInfo;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private EditText title_t,author_t,description_t;
    private TextView skip_t,currentTime_t,totalTime_t;
    private SeekBar seekBar;
    private Button cameraBtn,stopCameraBtn,imgBtn,movBtn,musicBtn,makeBtn,switchCameraBtn,enter_Btn,titleBtn,newBtn;
    private LinearLayout titlePlan,controlPlan,bufferIcon,timePlan;
    private FrameLayout recodePlan;
    private ImageView imgPreview;
    private GifView gifView;
    private SurfaceView surfaceView;
    private VideoView videoPreview;
    private MediaPlayer mPlayer,aPlayer;
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
    private String textMarkUrl="";//文字水印图
    private int file_type=0;
    private String recordFilename="testVideo";
    private String outputFilename="outputVideo";
    private String textMarkFilename="textMark";
    private Camera camera;
    private File videoFile;// 录制的视频文件
    private MediaRecorder mRecorder;
    private boolean hasCamera=false;
    private int camIdx=Camera.CameraInfo.CAMERA_FACING_BACK;
    private fileListener writeFileListener;
    private int currentTime=0;//当前已录制时间
    private int totalTime=0;//音乐文件总时间
    private Timer presTimer=new Timer();
    private TimerTask presTask;

    private final int IMAGE_FILE=1;
    private final int MUSIC_FILE=2;
    private final int VIDEO_FILE=3;
    private final int TIMECOUNT=4;
    private final int ENCODEING=8;
    private final int ENCODED=9;
    private final int SETFRAME=10;

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
        stopCameraBtn=(Button) findViewById(R.id.stopCameraBtn);
        switchCameraBtn=(Button) findViewById(R.id.switchCameraBtn);
        newBtn=(Button) findViewById(R.id.newBtn);
        titleBtn=(Button) findViewById(R.id.titleBtn);
        titlePlan=(LinearLayout) findViewById(R.id.titlePlan);
        controlPlan=(LinearLayout) findViewById(R.id.controlPlan);
        timePlan=(LinearLayout) findViewById(R.id.timePlan);
        imgBtn=(Button) findViewById(R.id.imgBtn);
        movBtn=(Button) findViewById(R.id.movBtn);
        musicBtn=(Button) findViewById(R.id.musicBtn);
        makeBtn=(Button) findViewById(R.id.makeBtn);
        imgPreview=(ImageView) findViewById(R.id.imgPreview);
        bufferIcon=(LinearLayout) findViewById(R.id.bufferIcon);
        recodePlan=(FrameLayout) findViewById(R.id.recodePlan);
        title_t=(EditText) findViewById(R.id.title_t);
        skip_t=(TextView) findViewById(R.id.skip_t);
        seekBar=(SeekBar) findViewById(R.id.seekBar);
        currentTime_t=(TextView) findViewById(R.id.currentTime_t);
        totalTime_t=(TextView) findViewById(R.id.totalTime_t);
        author_t=(EditText) findViewById(R.id.author_t);
        description_t=(EditText) findViewById(R.id.description_t);
        enter_Btn=(Button) findViewById(R.id.enter_Btn);
        videoPreview = (VideoView) this.findViewById(R.id.videoPreview);
        gifView = (GifView) findViewById(R.id.gifView);

        videoPreview.setVisibility(View.GONE);
        bufferIcon.setVisibility(View.GONE);
        makeBtn.setVisibility(View.GONE);
        controlPlan.setVisibility(View.GONE);
        recodePlan.setVisibility(View.GONE);
        stopCameraBtn.setVisibility(View.GONE);
        totalTime_t.setVisibility(View.GONE);
        seekBar.setVisibility(View.GONE);
        timePlan.setVisibility(View.GONE);
        newBtn.setVisibility(View.GONE);

        imgBtn.setOnClickListener(clickBtn);
        movBtn.setOnClickListener(clickBtn);
        musicBtn.setOnClickListener(clickBtn);
        makeBtn.setOnClickListener(clickBtn);
        switchCameraBtn.setOnClickListener(clickBtn);
        cameraBtn.setOnClickListener(clickBtn);
        stopCameraBtn.setOnClickListener(clickBtn);
        enter_Btn.setOnClickListener(clickBtn);
        titleBtn.setOnClickListener(clickBtn);
        skip_t.setOnClickListener(clickBtn);
        newBtn.setOnClickListener(clickBtn);

        DisplayMetrics dm =getResources().getDisplayMetrics();
        deviceInfo.screenWidth = dm.widthPixels;
        deviceInfo.screenHeight = dm.heightPixels;

        ViewGroup.LayoutParams lp = videoPreview.getLayoutParams();
        lp.width =deviceInfo.screenWidth;
        lp.height =deviceInfo.screenHeight;
        videoPreview.setLayoutParams(lp);
        //初始化播放器
//        mPlayer=new MediaPlayer();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {//UI处理
                switch (msg.what){
                    case SETFRAME:
                        imgPreview.setImageBitmap(imageBitmap);
                        break;
                    case ENCODEING:
                        bufferIcon.setVisibility(View.VISIBLE);
                        controlPlan.setVisibility(View.GONE);
                        break;
                    case ENCODED:
                        bufferIcon.setVisibility(View.GONE);
                        makeBtn.setVisibility(View.GONE);
                        newBtn.setVisibility(View.VISIBLE);
                        recodePlan.setVisibility(View.GONE);
                        controlPlan.setVisibility(View.GONE);
                        imgPreview.setVisibility(View.GONE);
                        //回放压好的文件
                        playVideo(outputUrl);
                        break;
                    case TIMECOUNT:
                        currentTime+=1;
                        currentTime_t.setText(mathFactory.ms2HMS(currentTime*1000));
                        if(aPlayer!=null && aPlayer.isPlaying()){
                            long _pec = currentTime * 100000 / totalTime;
                            seekBar.setProgress((int) _pec);
                        }
                        if(videoPreview!=null && videoPreview.isPlaying()){
                            long _pec = currentTime * 100000 / totalTime;
                            seekBar.setProgress((int) _pec);
                        }
//                        Log.d("LOGCAT","fileSize:"+videoFile.length());
                        break;
                    default:
                        break;
                }
            }
        };

        //输出文件地址
        outputUrl= FileUtil.getPath() + "/"+outputFilename+".mp4";
        //检测是否存在摄像头
        hasCamera=checkCameraHardware(context);
    }

    /**
     * 初始化surfaceView
     */
    private void initSurfaceView(){
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        sfHolder=surfaceView.getHolder();
        // 设置分辨率
        sfHolder.setFixedSize(deviceInfo.screenWidth, deviceInfo.screenHeight);
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
//                Log.d("LOGCAT", "surfaceChanged");
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
            cP.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//连续对焦
//            camera.cancelAutoFocus();//连续对焦时必须加这个
            camera.setParameters(cP);
            videoUtils.prviewSizeList = cP.getSupportedPreviewSizes();
            videoUtils.videoSizeList = cP.getSupportedVideoSizes();
            //降序排列
            Collections.sort(videoUtils.prviewSizeList, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size lhs, Camera.Size rhs) {
                    if (lhs.width > rhs.width) {
                        return -1;
                    } else if (lhs.width == rhs.width) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
            deviceInfo.cameraWidth=videoUtils.prviewSizeList.get(1).width;
            deviceInfo.cameraHeight=videoUtils.prviewSizeList.get(1).height;
            cP.setPreviewSize(deviceInfo.cameraWidth,deviceInfo.cameraHeight);
            Log.i("LOGCAT","camera:"+deviceInfo.cameraWidth+"-"+deviceInfo.cameraHeight);
            camera.setPreviewDisplay(sfHolder);//通过SurfaceView显示取景画面
            camera.startPreview(); //开始预览
            //获得最接近预览分辨率的视频分辨率支持值
            int recodeSizeIndex=videoUtils.bestVideoSize(deviceInfo.cameraWidth);
            deviceInfo.recodeWidth=videoUtils.videoSizeList.get(recodeSizeIndex).width;
            deviceInfo.recodeHeight=videoUtils.videoSizeList.get(recodeSizeIndex).height;
            Log.i("LOGCAT","recode:"+deviceInfo.recodeWidth+"-"+deviceInfo.recodeHeight);
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
                    stopPlayer();
                    startRecode();
                    break;
                case R.id.stopCameraBtn:
                    stopRecode();
                    break;
                case R.id.imgBtn:
                    file_type=IMAGE_FILE;
                    chooseFile();
                    break;
                case R.id.musicBtn:
                    file_type=MUSIC_FILE;
                    chooseFile();
                    break;
                case R.id.movBtn:
//                    Toast.makeText(context, "暂未开放！", Toast.LENGTH_SHORT).show();
                    file_type=VIDEO_FILE;
                    chooseFile();
                    break;
                case R.id.makeBtn:
                    //开始合并
                    makeVideo();
                    recodePlan.setVisibility(View.GONE);
                    break;
                case R.id.enter_Btn:
                    videoInfo.vTitle=title_t.getText().toString();
                    videoInfo.author=author_t.getText().toString();
                    videoInfo.description=description_t.getText().toString();
                    titlePlan.setVisibility(View.GONE);
                    controlPlan.setVisibility(View.VISIBLE);
                    recodePlan.setVisibility(View.VISIBLE);
                    //先生成文字水印图片
                    final String[] _info={videoInfo.vTitle,videoInfo.author,videoInfo.description};
                    if(videoInfo.vTitle!=null &&! videoInfo.vTitle.equals("")) {
                        textMarkUrl=FileUtil.getPath() + "/" + textMarkFilename + ".png";
                        Runnable bmpR=new Runnable() {
                            @Override
                            public void run() {
                                bitmapFactory.writeImage(textMarkUrl,_info);
                            }
                        };
                        ThreadPoolUtils.execute(bmpR);
                    }
                    break;
                case R.id.skip_t:
                    titlePlan.setVisibility(View.GONE);
                    controlPlan.setVisibility(View.VISIBLE);
                    recodePlan.setVisibility(View.VISIBLE);
                    break;
                case R.id.titleBtn:
                    titlePlan.setVisibility(View.VISIBLE);
                    controlPlan.setVisibility(View.GONE);
                    recodePlan.setVisibility(View.GONE);
                    break;
                case R.id.newBtn:
                    controlPlan.setVisibility(View.VISIBLE);
                    recodePlan.setVisibility(View.VISIBLE);
                    imgPreview.setVisibility(View.VISIBLE);
                    newBtn.setVisibility(View.GONE);
                    stopPlayer();
                    stopPresTimer();
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
        // 创建保存录制视频的视频文件
        videoFile = new File(FileUtil.getPath() + "/"+recordFilename+".mp4");
        videoUrl=FileUtil.getPath() + "/"+recordFilename+".mp4";
        if(videoFile!=null && videoFile.exists()){
            videoFile.delete();//录制前先删除原文件，不然文件大小不会变。
        }
        cameraBtn.setVisibility(View.GONE);
        stopCameraBtn.setVisibility(View.VISIBLE);
        controlPlan.setVisibility(View.GONE);
        //录制
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        if(audioUri!=null) {
            playMusic(musicUrl);
        }
        timePlan.setVisibility(View.VISIBLE);
        try {
            Log.i("LOGCAT", "Start recording...");
            currentTime=0;
            switchCameraBtn.setVisibility(View.GONE);
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
            //设置分辨率
            mRecorder.setVideoSize(deviceInfo.recodeWidth,deviceInfo.recodeHeight);
            //码率
            mRecorder.setVideoEncodingBitRate(5*deviceInfo.recodeWidth*deviceInfo.recodeHeight);
            // 每秒24帧
//            mRecorder.setVideoFrameRate(24);
            mRecorder.setOutputFile(videoFile.getAbsolutePath());
            // 指定使用SurfaceView来预览视频
            mRecorder.setPreviewDisplay(sfHolder.getSurface());
            //这个监听可以用来控制录制时长
//            mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
//                @Override
//                public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
//                    Log.d("LOGCAT", "RecordService got MediaRecorder onInfo callback with what: " + i + " extra: " + i1);
//                }
//            });
            mRecorder.prepare();
            // 开始录制
            mRecorder.start();
            //监听文件的写入
            writeFileListener = new fileListener(videoFile.getPath(),this);
            writeFileListener.startWatching();
            startPresTimer();
        }catch (IOException e){
        }
    }

    /**
     * 停止录制
     */
    private void stopRecode(){
        stopCameraBtn.setVisibility(View.GONE);
        cameraBtn.setVisibility(View.VISIBLE);
        controlPlan.setVisibility(View.VISIBLE);
        makeBtn.setVisibility(View.VISIBLE);
        stopPresTimer();
        seekBar.setProgress(0);
        timePlan.setVisibility(View.GONE);
        switchCameraBtn.setVisibility(View.VISIBLE);
        stopPlayer();
        // 设置该组件让屏幕不会自动关闭
        surfaceView.getHolder().setKeepScreenOn(false);
        Log.i("LOGCAT", "录制完毕， 存储为 " + videoFile.getPath());
        // 停止录制
        mRecorder.stop();
        // 释放资源
        mRecorder.release();
        mRecorder = null;
        //回放刚刚录制的视频
//        playVideo(videoUrl);
    }

    /**
     * 打开文件
     */
    private void chooseFile(){
        if(Build.VERSION.SDK_INT >= 24){//7.0
        }else if(Build.VERSION.SDK_INT >= 19){//4.4
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.d("LOGCAT", "file type:" + file_type);
        switch (file_type) {
            case IMAGE_FILE:
//                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                break;
            case MUSIC_FILE:
//                intent.setData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                intent.setType("audio/*");
                break;
            case VIDEO_FILE:
//                intent.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                break;
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a File"), 0x1);
    }

    /**
     * 监听文件选择
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x1 && resultCode == Activity.RESULT_OK && data!=null) {
            switch (file_type){
                case IMAGE_FILE:
                    try{
                        imageUri = data.getData();
                        Log.i("LOGCAT", "uri path:"+imageUri.getPath()+"   "+imageUri.toString());

                        imageUrl=FileUtil.getPath(this,imageUri);
                        gifView.setShowDimension(deviceInfo.screenHeight,deviceInfo.screenWidth);
                        if(!imageUrl.equals("")) {
                            String _type=imageUrl.substring(imageUrl.length()-3);
                            Log.d("LOGCAT","imageType:"+_type+"_"+deviceInfo.screenWidth+"_"+deviceInfo.screenHeight);
                            if(_type.equals("gif")){
                                gifView.setGifImage(new FileInputStream(imageUrl));
                            }else {
                                imgPreview.setVisibility(View.VISIBLE);
                                Runnable bmpR = new Runnable() {
                                    @Override
                                    public void run() {
                                        imageBitmap = imageLoader.returnBitMapLocal(imageUrl, 300, 200);
                                        if (imageBitmap != null) {
                                            Message msg = new Message();
                                            msg.what = SETFRAME;
                                            mHandler.sendMessage(msg);
                                        }
                                    }
                                };
                                ThreadPoolUtils.execute(bmpR);
                            }
                        }
                    }catch (Exception e){
                        Log.i("LOGCAT",e.toString());
                    }
                    break;
                case MUSIC_FILE:
                    try{
                        audioUri = data.getData();
                        musicUrl=FileUtil.getPath(this,audioUri);
                        if(!musicUrl.equals("")) {
                            seekBar.setVisibility(View.VISIBLE);
                            totalTime_t.setVisibility(View.VISIBLE);
                            Log.i("LOGCAT", "path:" + musicUrl);
                        }
                    }catch (Exception e){
                        Log.i("LOGCAT",e.toString());
                    }
                    break;
                case VIDEO_FILE:
                    try{
                        videoUri = data.getData();
                        Log.i("LOGCAT", "uri path:"+videoUri.getPath()+"   "+videoUri.toString());
                        imageUrl=FileUtil.getPath(this,videoUri);//暂时视频背景和图片背景只能2选1，所以这里赋值给imageUrl
                        if(!imageUrl.equals("")) {
                            Log.i("LOGCAT", "path:" + imageUrl);
                            //暂时无法播放
//                            playVideo(videoUrl);
                            Toast.makeText(this, "暂时无法预览，但支持合成", Toast.LENGTH_SHORT).show();
                            imgPreview.setVisibility(View.GONE);
                        }
                    }catch (Exception e){
                        Log.i("LOGCAT",e.toString());
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
                videoPreview.setVisibility(View.VISIBLE);
                videoPreview.setZOrderMediaOverlay(true);
                Uri uri = Uri.parse(_url);
                videoPreview.setVideoURI(uri);
                currentTime=0;
                videoPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        totalTime=videoPreview.getDuration();
                        totalTime_t.setText(mathFactory.ms2HMS(totalTime));
                        timePlan.setVisibility(View.VISIBLE);
                        seekBar.setVisibility(View.VISIBLE);
                        totalTime_t.setVisibility(View.VISIBLE);
                        videoPreview.start();
                        startPresTimer();
                    }
                });
                videoPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        currentTime=0;
                        videoPreview.seekTo(0);
                        videoPreview.start();
                    }
                });
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
                        totalTime=aPlayer.getDuration();
                        totalTime_t.setText(mathFactory.ms2HMS(totalTime));
                        Log.d("LOGCAT","audioPlayer totalTime:"+totalTime);
                        aPlayer.start();
                    }
                });
                //音乐播放完毕结束录制
                aPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        stopRecode();
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
        surfaceView.setZOrderMediaOverlay(true);
        videoPreview.setVisibility(View.GONE);
        timePlan.setVisibility(View.GONE);
        if(aPlayer!=null && aPlayer.isPlaying()){
            aPlayer.stop();
            aPlayer.release();
            aPlayer=null;
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
     * ffmpeg操作
     */
    private void makeVideo(){
        if(imageUri==null && audioUri==null && textMarkUrl.equals("")) {
            Toast.makeText(this, "少年，不加点什么吗？", Toast.LENGTH_SHORT).show();
            return;
        }
//        String[] commands= ffmpegCommandCentre.addTextMark(textMarkUrl,videoUrl,outputUrl);
        String[] commands= ffmpegCommandCentre.makeVideo(textMarkUrl,imageUrl,musicUrl,videoUrl,outputUrl,currentTime);
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
                        msg.what = ENCODED;
                        mHandler.sendMessage(msg);
                    }
                });
            }
        };
        ThreadPoolUtils.execute(compoundRun);
    }

    /**
     * 确保视频保保存完毕后才可执行合成操作，否则可能引发ffmpeg的空指针错误
     */
    public void fileClosed(){
        makeBtn.setVisibility(View.VISIBLE);
        writeFileListener.stopWatching();
    }

    /**
     * 监听文件大小
     */
    private void waitForWirtenCompleted(File file) {
        if (!file.exists())
            return;
        long old_length;
        do {
            old_length = file.length();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("LOGCAT", "filesize:"+old_length + " " + file.length());
        } while (old_length != file.length());
    }

    /**
     * 计时器的开始和关闭
     */
    private void startPresTimer(){
        presTask = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(mHandler!=null) {
                    Message message = new Message();
                    message.what = TIMECOUNT;
                    mHandler.sendMessage(message);
                }
            }
        };
        if(presTimer==null){
            presTimer=new Timer();
        }
        presTimer.schedule(presTask, 1000, 1000);
    }
    private void stopPresTimer(){
        seekBar.setVisibility(View.GONE);
        totalTime_t.setVisibility(View.GONE);
        if(presTimer!=null) {
            presTimer.cancel();
            presTimer=null;
        }
        if(presTask!=null) {
            presTask.cancel();
            presTask=null;
        }
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
