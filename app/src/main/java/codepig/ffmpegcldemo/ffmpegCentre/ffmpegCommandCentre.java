package codepig.ffmpegcldemo.ffmpegCentre;

import android.util.Log;

import java.util.ArrayList;

import codepig.ffmpegcldemo.config.deviceInfo;
import codepig.ffmpegcldemo.config.videoSetting;

/**
 * 拼装ffmpeg命令行文本
 * Created by QZD on 2017/4/18.
 */

public class ffmpegCommandCentre {
    /**
     * 图片水印
     */
    public static String[] addImageMark(String imageUrl,String videoUrl,String outputUrl){
//        Log.d("LOGCAT","add picmask");
        String[] commands = new String[9];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = videoUrl;
        //水印
        commands[3] = "-i";
        commands[4] = imageUrl;//此处的图片地址换成带透明通道的视频就可以合成动态视频遮罩。
        commands[5] = "-filter_complex";
        commands[6] = "[1:v]scale="+ deviceInfo.screenWidth+":"+deviceInfo.screenHeight+"[s];[0:v][s]overlay=0:0";
        //覆盖输出
        commands[7] = "-y";//直接覆盖输出文件
        //输出文件
        commands[8] = outputUrl;
        return commands;
    }

    /**
     * 背景音乐
     */
    public static String[] addMusic(String musicUrl,String videoUrl,String outputUrl){
//        Log.d("LOGCAT","add music");
        String[] commands = new String[7];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = videoUrl;
        //音乐
        commands[3] = "-i";
        commands[4] = musicUrl;
        //覆盖输出
        commands[5] = "-y";
        //输出文件
        commands[6] = outputUrl;
        return commands;
    }

    /**
     * 文字水印
     */
    public static String[] addTextMark(String imageUrl,String videoUrl,String outputUrl){
        ArrayList<String> _commands=new ArrayList<>();
        _commands.add("ffmpeg");
        //输入
        _commands.add("-i");
        _commands.add(videoUrl);
        //水印
        _commands.add("-i");
        _commands.add(imageUrl);
        _commands.add("-filter_complex");
        _commands.add("overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2");
        //覆盖输出
        _commands.add("-y");//直接覆盖输出文件
        //输出文件
        _commands.add(outputUrl);
        String[] commands = new String[_commands.size()];
        for(int i=0;i<_commands.size();i++){
            commands[i]=_commands.get(i);
        }
        return commands;
    }

    /**
     *  拼接视频
     */
    public static String[] concatVideo(String _filePath, String  _outPath){//-f concat -i list.txt -c copy concat.mp4
        ArrayList<String> _commands=new ArrayList<>();
        _commands.add("ffmpeg");
        //输入
        _commands.add("-f");
        _commands.add("concat");
        _commands.add("-i");
        _commands.add(_filePath);
        //输出文件
        _commands.add("-c");
        _commands.add("copy");
        _commands.add(_outPath);
        String[] commands = new String[_commands.size()];
        String _pr="";
        for(int i=0;i<_commands.size();i++){
            commands[i]=_commands.get(i);
            _pr+=commands[i];
        }
        Log.d("LOGCAT","ffmpeg command:"+_pr+"-"+commands.length);
        return commands;
    }

    /**
     * 图片转视频
     */
    public static String[] image2mov(String imageUrl,String _t,String outputUrl){
        ArrayList<String> _commands=new ArrayList<>();
        _commands.add("ffmpeg");
        //输入
        String _type=imageUrl.substring(imageUrl.length()-3);
        if(_type.equals("gif")){
            _commands.add("-ignore_loop");
            _commands.add("0");
        }else{
            _commands.add("-loop");//将单张图片循环,如果是gif图片则是忽略此参数的
            _commands.add("1");
        }
        _commands.add("-i");
        _commands.add(imageUrl);
        //视频配置
//        _commands.add("-vcodec");
//        _commands.add("libx264");
        _commands.add("-r");
        _commands.add("25");
        _commands.add("-b");
        _commands.add("200k");
        _commands.add("-s");
        _commands.add("640x360");
        _commands.add("-t");
        _commands.add(_t);
        //覆盖输出
        _commands.add("-y");//直接覆盖输出文件
        //输出文件
        _commands.add(outputUrl);
        String[] commands = new String[_commands.size()];
        String _pr="";
        for(int i=0;i<_commands.size();i++){
            commands[i]=_commands.get(i);
            _pr+=commands[i];
        }
        Log.d("LOGCAT","ffmpeg command:"+_pr+"-"+commands.length);
        return commands;
    }

    /**
     * 合成
     */
    public static String[] makeVideo(String textIimageUrl,String imageUrl,String musicUrl,String videoUrl,String outputUrl,int _duration){
//        Log.d("LOGCAT","add pic and music");
        ArrayList<String> _commands=new ArrayList<>();
        _commands.add("ffmpeg");
        //输入
        _commands.add("-i");
        _commands.add(videoUrl);
        if(!textIimageUrl.equals("") || !imageUrl.equals("")){
            //图片水印
            if(!imageUrl.equals("")) {
                _commands.add("-ignore_loop");
                _commands.add("0");
                _commands.add("-i");
                _commands.add(imageUrl);//此处的图片地址换成带透明通道的视频就可以合成动态视频遮罩。
            }
            //文字水印
            if(!textIimageUrl.equals("")){
                _commands.add("-i");
                _commands.add(textIimageUrl);
            }
            _commands.add("-filter_complex");
            if(textIimageUrl.equals("")){
                _commands.add("[1:v]scale=" + deviceInfo.screenWidth + ":" + deviceInfo.screenHeight + "[s];[0:v][s]overlay=0:0");
            }else if(imageUrl.equals("")) {
                _commands.add("overlay=x='if(lte(t,"+videoSetting.titleDuration+"),(main_w-overlay_w)/2,NAN )':(main_h-overlay_h)/2");
            }else{
                _commands.add("[1:v]scale=" + deviceInfo.screenWidth + ":" + deviceInfo.screenHeight + "[img1];[2:v]scale=" + videoSetting.titlePicWidth + ":" + videoSetting.titlePicHeight + "[img2];[0:v][img1]overlay=0:0[bkg];[bkg][img2]overlay=x='if(lte(t,"+videoSetting.titleDuration+"),(main_w-overlay_w)/2,NAN )':(main_h-overlay_h)/2");
            }
        }
        //音乐
        if(!musicUrl.equals("")) {
            //-ss和-t参数控制音频长度
//            _commands.add("-ss");
//            _commands.add("00:00:00");
//            _commands.add("-t");
//            _commands.add(""+_duration);
            _commands.add("-i");
            _commands.add(musicUrl);
        }
        _commands.add("-r");
        _commands.add("25");
        _commands.add("-b");
        _commands.add("1000k");
        _commands.add("-s");
        _commands.add("640x360");
        //覆盖输出
//        _commands.add("-y");
        //时间参数，没有的话如果视频短于音频则会在视频最后一帧停住继续合成直到音频结束。
        _commands.add("-ss");
        _commands.add("00:00:00");
        _commands.add("-t");
        _commands.add(""+_duration);
        //输出文件
        _commands.add(outputUrl);
        String[] commands = new String[_commands.size()];
        String _pr="";
        for(int i=0;i<_commands.size();i++){
            commands[i]=_commands.get(i);
            _pr+=commands[i];
        }
        Log.d("LOGCAT","ffmpeg command:"+_pr+commands.length);
        return commands;
    }

    public static void reset(){
        //
    }
}
