package codepig.ffmpegcldemo.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URISyntaxException;

public class FileUtil {
    public static final String LOG_TAG = "LOGCAT";
    public static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
    public static String packageFilesDirectory = null;
    public static String storagePath = null;
    private static String mDefaultFolder = "littleMVer";

    public static void setDefaultFolder(String defaultFolder) {
        mDefaultFolder = defaultFolder;
    }

    public static String getPath() {
        return getPath(null);
    }

    public static String getPath(Context context) {
        if(storagePath == null) {
            storagePath = externalStorageDirectory.getAbsolutePath() + "/" + mDefaultFolder;
            File file = new File(storagePath);
            if(!file.exists()) {
                if(!file.mkdirs()) {
                    storagePath = getPathInPackage(context, true);
                }
            }
        }
        return storagePath;
    }

    public static String getPathInPackage(Context context, boolean grantPermissions) {
        if(context == null || packageFilesDirectory != null)
            return packageFilesDirectory;
        //手机不存在sdcard, 需要使用 data/data/name.of.package/files 目录
        String path = context.getFilesDir() + "/" + mDefaultFolder;
        File file = new File(path);
        if(!file.exists()) {
            if(!file.mkdirs()) {
                Log.e(LOG_TAG, "在pakage目录创建CGE临时目录失败!");
                return null;
            }
            if(grantPermissions) {
                //设置隐藏目录权限.
                if (file.setExecutable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is executable");
                }
                if (file.setReadable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is readable");
                }
                if (file.setWritable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is writable");
                }
            }
        }

        packageFilesDirectory = path;
        return packageFilesDirectory;
    }

    public static void saveTextContent(String text, String filename) {
        Log.i(LOG_TAG, "Saving text : " + filename);
        try {
            FileOutputStream fileout = new FileOutputStream(filename);
            fileout.write(text.getBytes());
            fileout.flush();
            fileout.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
        }
    }

    public static String getTextContent(String filename) {
        Log.i(LOG_TAG, "Reading text : " + filename);
        if(filename == null) {
            return null;
        }
        String content = "";
        byte[] buffer = new byte[256]; //Create cache for reading.
        try {
            FileInputStream filein = new FileInputStream(filename);
            int len;
            while(true) {
                len = filein.read(buffer);
                if(len <= 0)
                    break;
                content += new String(buffer, 0, len);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
            return null;
        }
        return content;
    }

    /**
     * 获取文件绝对路径,4.4系统之前和之后返回的结构不同，所以要不同处理。这里要用到DocumentProvider
     * @param context
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {//4.4以后
            if (isExternalStorageDocument(uri)) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {// DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {//4.2.2以后4.4以前，判断协议是以content://开头还是file://开头
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {//4.2.2以前
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
