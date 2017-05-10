package codepig.ffmpegcldemo.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.FileOutputStream;

/**
 * 生成图片
 * Created by QZD on 2017/5/8.
 */

public class bitmapFactory {
    private static int textScale=5;
    private static int imageH=15;//图片高
    private static int imageW=270;//图片宽
    private static float brushSize=100;//画笔粗细
    private static int brushColor=Color.WHITE;//画笔颜色
    private static int bgColor=Color.TRANSPARENT;//背景颜色
    private static int imageQuality=100;//图片压缩质量

    /**
     * 设置图片大小
     */
    public static void sizeConfig(int _lh,int _w){
        imageH=_lh;
        imageW=_w;
    }

    /**
     * 设置样式
     * @param _lh
     * @param _w
     * @param _brushS
     * @param _brushC
     */
    public static void imageConfig(float _brushS,int _brushC,int _bgC){
        brushSize=_brushS;
        brushColor=_brushC;
        bgColor=_bgC;
    }

    /**
     * 绘制图片
     * @param path 生成图片的地址
     * @param _msg 文字
     * @return
     */
    public static boolean writeImage(String path,String _msg){
        try {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);//Paint.ANTI_ALIAS_FLAG参数开启抗锯齿
            p.setColor(brushColor);
            p.setTextSize(brushSize);

            //创建一个矩形来获取文字区域宽高，作为图片大小
            Rect rect = new Rect();
            p.getTextBounds(_msg,0,_msg.length(),rect);
            imageW = rect.width();
            imageH = rect.height();
//            Log.d("LOGCAT",_msg+"---text size:"+imageW+"-"+imageH);

            Bitmap bitmap = Bitmap.createBitmap(imageW, imageH, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(bgColor);
            canvas.drawText(_msg, 0, imageH-1, p);//注意这里的y参数是baseline的位置而不是文字开始或中心的位置

            Log.d("LOGCAT", "path:"+path);
            //将Bitmap保存为png图片
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, imageQuality, out);
            Log.d("LOGCAT", "png done");
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            Log.d("LOGCAT", "e:"+e.toString());
            e.printStackTrace();
        }
        return false;
    }
}
