package codepig.ffmpegcldemo.ffmpegCentre;

import android.util.Log;

import codepig.ffmpegcldemo.config.deviceInfo;

/**
 * 拼装ffmpeg命令行文本
 * Created by QZD on 2017/4/18.
 */

public class ffmpegCommandCentre {
    /**
     * 图片水印
     */
    public static String[] addwaterMark(String imageUrl,String videoUrl,String outputUrl){
        Log.d("LOGCAT","add picmask");
        String[] commands = new String[9];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = videoUrl;
        //水印
        commands[3] = "-i";
        commands[4] = imageUrl;//此处的图片地址换成带透明通道的视频就可以合成动态视频遮罩。
        commands[5] = "-filter_complex";
//        commands[6] = "overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2";
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
        Log.d("LOGCAT","add music");
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
     * 水印加图片
     */
    public static String[] addPicAndMusic(String imageUrl,String musicUrl,String videoUrl,String outputUrl){
        Log.d("LOGCAT","add pic and music");
        String[] commands = new String[12];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = videoUrl;
        //水印
        commands[3] = "-i";
        commands[4] = imageUrl;//此处的图片地址换成带透明通道的视频就可以合成动态视频遮罩。
        commands[5] = "-filter_complex";
        commands[6] = "overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2";
        commands[7] = "scale=100:200";
        //音乐
        commands[8] = "-i";
        commands[9] = musicUrl;
        //覆盖输出
        commands[10] = "-y";
        //输出文件
        commands[11] = outputUrl;
        return commands;
    }
}
