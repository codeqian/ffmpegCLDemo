package codepig.ffmpegcldemo.listener;

import android.os.FileObserver;
import android.util.Log;

import codepig.ffmpegcldemo.MainActivity;

/**
 * 文件监听类
 * Created by QZD on 2017/4/14.
 */

public class fileListener extends FileObserver {
    private MainActivity parentActivity;
    public fileListener (String path, MainActivity mainActivity) {
        super(path);
        parentActivity=mainActivity;
    }

    @Override
    public void onEvent(int event, String path) {
        switch(event){
            case android.os.FileObserver.ALL_EVENTS:
                //所有事件 相当于default的功能
                Log.d("LOGCAT","fileListener:"+path);
                break;
            case android.os.FileObserver.CREATE:
                //文件被创建
                Log.d("LOGCAT","fileListener: file create");
                break;
            case android.os.FileObserver.OPEN :
                //文件被打开
                Log.d("LOGCAT","fileListener: file open");
                break;
            case android.os.FileObserver.ACCESS:
                //打开文件后，读文件内容操作 文件或目录被访问
                Log.d("LOGCAT","fileListener: file access");
                break;
            case android.os.FileObserver.MODIFY:
                //文件被修改
//                Log.d("LOGCAT","fileListener: file modufy");
                break;
            case android.os.FileObserver.ATTRIB:
                //未明操作
                break;
            case android.os.FileObserver.CLOSE_NOWRITE:
                //没有编辑文件，关闭
                Log.d("LOGCAT","fileListener: file nowrite");
                break;
            case android.os.FileObserver.CLOSE_WRITE:
                //编辑完文件，关闭
                Log.d("LOGCAT","fileListener: file close");
                parentActivity.fileClosed();
                break;
            case android.os.FileObserver.DELETE:
                //文件被删除
                Log.d("LOGCAT","fileListener: file delete");
                break;
            case android.os.FileObserver.MOVED_FROM:
                //文件被移动
                Log.d("LOGCAT","fileListener: file move");
                break;
        }
    }
}
