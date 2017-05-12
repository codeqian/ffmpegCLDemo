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
        commands[6] = "[1:v]scale="+ deviceInfo.screenWtdth+":"+deviceInfo.screenHeight+"[s];[0:v][s]overlay=0:0";
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
     * 合成
     */
    public static String[] makeVideo(String textIimageUrl,String imageUrl,String musicUrl,String videoUrl,String outputUrl,int _duration){
//        Log.d("LOGCAT","add pic and music");
        ArrayList<String> _commands=new ArrayList<>();
        _commands.add("ffmpeg");
        //输入
        _commands.add("-i");
        _commands.add(videoUrl);
        //文字水印
        if(!textIimageUrl.equals("") || !imageUrl.equals("")){
            //图片水印
            if(!imageUrl.equals("")) {
                _commands.add("-i");
                _commands.add(imageUrl);//此处的图片地址换成带透明通道的视频就可以合成动态视频遮罩。
            }
            if(!textIimageUrl.equals("")){
                _commands.add("-ss");
                _commands.add("00:00:00");
                _commands.add("-t");
                _commands.add(""+videoSetting.titleDuration);
                _commands.add("-i");
                _commands.add(textIimageUrl);
            }
            _commands.add("-filter_complex");
            if(textIimageUrl.equals("")){
                _commands.add("[1:v]scale=" + deviceInfo.screenWtdth + ":" + deviceInfo.screenHeight + "[s];[0:v][s]overlay=0:0");
            }else if(imageUrl.equals("")) {
                _commands.add("overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2");
            }else{
                _commands.add("[1:v]scale=" + deviceInfo.screenWtdth + ":" + deviceInfo.screenHeight + "[img1];[2:v]scale=400:200[img2];[0:v][img1]overlay=0:0[bkg];[bkg][img2]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2");
            }
        }
        //音乐
        if(!musicUrl.equals("")) {
            //-ss和-t参数控制音频长度
            _commands.add("-ss");
            _commands.add("00:00:00");
            _commands.add("-t");
            _commands.add(""+_duration);
            _commands.add("-i");
            _commands.add(musicUrl);

        }
        //覆盖输出
        _commands.add("-y");
        //输出文件
        _commands.add(outputUrl);
        String[] commands = new String[_commands.size()];
        String _pr="";
        for(int i=0;i<_commands.size();i++){
            commands[i]=_commands.get(i);
            _pr+=commands[i];
        }
        Log.d("LOGCAT","ffmpeg command:"+_pr);
        return commands;
    }
}
