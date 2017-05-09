package codepig.ffmpegcldemo.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * 图片处理
 * Created by QZD on 2017/5/8.
 */

public class bitmapFactory {
    private static int lineHeight=20;
    private static int imageW=270;
    private static float brushSize=15;
    private static int brushColor=Color.BLACK;
    private static int imageQuality=90;

    /**
     * 设置样式
     * @param _lh
     * @param _w
     * @param _brushS
     * @param _brushC
     */
    public static void imageConfig(int _lh,int _w,float _brushS,int _brushC){
        lineHeight=_lh;
        imageW=_w;
        brushSize=_brushS;
        brushColor=_brushC;
    }

    /**
     * 绘制图片
     * @param path
     * @param data 文字列表
     * @return
     */
    public static boolean writeImage(String path,ArrayList<String> data){
        try {
            int height = data.size()*lineHeight;     //图片高
            Bitmap bitmap = Bitmap.createBitmap(imageW, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.TRANSPARENT);   //背景颜色

            Paint p = new Paint();
            p.setColor(brushColor);   //画笔颜色
            p.setTextSize(brushSize);         //画笔粗细
            for(int i=0;i<data.size();i++){
                canvas.drawText(data.get(i), lineHeight, (i+1)*lineHeight, p);
            }

            Log.d("path", path);
            //将Bitmap保存为png图片
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, imageQuality, out);
            Log.e("done", "done");
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return false;
    }
}
