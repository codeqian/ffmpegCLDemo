package codepig.ffmpegcldemo.ffmpegCentre;

import android.util.Log;

import java.io.File;

import codepig.ffmpegcldemo.config.deviceInfo;
import codepig.ffmpegcldemo.math.mathFactory;

/**
 * 拼装ffmpeg命令行文本
 * Created by QZD on 2017/4/18.
 */

public class ffmpegCommandCentre {
    /**
     * 图片水印
     */
    public static String[] addwaterMark(String imageUrl,String videoUrl,String outputUrl){
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
     * 图片水印加音乐
     */
    public static String[] addPicAndMusic(String imageUrl,String musicUrl,String videoUrl,String outputUrl){
//        Log.d("LOGCAT","add pic and music");
        String[] commands = new String[11];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = videoUrl;
        //水印
        commands[3] = "-i";
        commands[4] = imageUrl;//此处的图片地址换成带透明通道的视频就可以合成动态视频遮罩。
        commands[5] = "-filter_complex";
        commands[6] = "[1:v]scale="+ deviceInfo.screenWtdth+":"+deviceInfo.screenHeight+"[s];[0:v][s]overlay=0:0";
        //音乐
        commands[7] = "-i";
        commands[8] = musicUrl;
        //覆盖输出
        commands[9] = "-y";
        //输出文件
        commands[10] = outputUrl;
        return commands;
    }

    /**
     * 时间戳水印
     */
    public static String[] addTimeMark(String videoUrl,String outputUrl){
        File fontFile = new File("/system/fonts/DroidSans.ttf");
        if (fontFile.exists()){
            Log.d("LOGCAT","has font");
        }else{
            Log.d("LOGCAT","no font");
        }
        Log.d("LOGCAT","add timeMark");
        String timeMark= mathFactory.currentTimeInDate();
        String[] commands = new String[8];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = videoUrl;
        //水印
//        commands[3] = "-vf";
        commands[3] = "-i";
        commands[4] = "-filter_complex";
//        commands[4] = "drawtext=text="+mathFactory.currentTimeInDate()+":fontfile=/usr/share/fonts/truetype/ttf-indic-fonts-core/utkal.ttf:x=100:y=x/dar:fontsize=24:fontcolor=white@0.7";
        commands[5] = "drawtext=fontfile='" + fontFile.getAbsolutePath() + "':text='this is awesome':x=0:y=50:fontsize=24:fontcolor=white";
        //覆盖输出
        commands[6] = "-y";
        //输出文件
        commands[7] = outputUrl;
        return commands;
    }

//    "-filter_complex", "[0:v]pad=iw:ih+200:0:(oh-ih)/2:color=white[v0];[v0]drawtext=fontsize=60:fontfile=/system/fonts/DroidSans.ttf:fontcolor=black:text=THIS IS:x=(w-tw)/2:y=(50-th)/2[v1];[v1]drawtext=fontsize=60:fontfile=/system/fonts/DroidSans.ttf:fontcolor=black:text=SPARTA!:x=(w-tw)/2:y=h-25-(th/2)",
//            "-acodec", "copy", final_output2};
//    ffmpeg -i output5.mp4 -vf "drawtext=text='lihuibin':fontfile=/usr/share/fonts/truetype/ttf-indic-fonts-core/utkal.ttf:fontsize=24:fontcolor=red@0.8:x=w-tw-20:y=h-th-20" -c:v libx264 -c:a copy output8.mp4
}
