package codepig.ffmpegcldemo;

public class FFmpegKit {
    static{
        System.loadLibrary("ffmpeg");
        System.loadLibrary("ffmpeginvoke");
    }
    public native static int run(String[] commands);
}