package codepig.ffmpegcldemo.utils;

import android.hardware.Camera;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 视频相关设置处理
 * Created by QZD on 2017/5/22.
 */

public class videoUtils {
    public static List<Camera.Size> prviewSizeList;
    public static List<Camera.Size> videoSizeList;
    public static int bestVideoSize(int _w){
        //降序排列
        Collections.sort(videoUtils.videoSizeList, new Comparator<Camera.Size>() {
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
        for(int i=0;i<videoSizeList.size();i++){
            if(videoSizeList.get(i).width<=_w){
                return i;
            }
        }
        return 0;
    }
}
