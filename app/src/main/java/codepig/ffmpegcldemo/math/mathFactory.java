package codepig.ffmpegcldemo.math;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 转换数字格式
 * Created by QZD on 2015/2/26.
 */
public class mathFactory {
    private static String[] tKey={"01136c5948d353b1bg2","01136c5948d353b1bg2rfj"};//获取hls时用的校验key.获取下载时用的校验key

    /**
     * 将毫秒转换为小时：分钟：秒格式
     * @param _ms
     * @return
     */
    public static String ms2HMS(int _ms){
        String HMStime;
        _ms/=1000;
        int hour=_ms/3600;
        int mint=(_ms%3600)/60;
        int sed=_ms%60;
        String hourStr= String.valueOf(hour);
        if(hour<10){
            hourStr="0"+hourStr;
        }
        String mintStr= String.valueOf(mint);
        if(mint<10){
            mintStr="0"+mintStr;
        }
        String sedStr= String.valueOf(sed);
        if(sed<10){
            sedStr="0"+sedStr;
        }
        HMStime=hourStr+":"+mintStr+":"+sedStr;
        return HMStime;
    }

    /**
     * 将毫秒转换为标准日期格式
     * @param _ms
     * @return
     */
    public static String ms2Date(long _ms){
        Date date = new Date(_ms);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(date);
    }

    public static String ms2DateOnlyDay(long _ms){
        Date date = new Date(_ms);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format.format(date);
    }

    /**
     * 标准时间转换为时间戳
     * @param _data
     * @return
     */
    public static long Date2ms(String _data){
        SimpleDateFormat format =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(_data);
            return date.getTime();
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * 获取当前时间的标准格式
     */
    public static String currentTimeInDate(){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());
            return formatter.format(curDate);
        }catch (Exception e){
        }
        return null;
    }

    /**
     * 将大于1万的数字转换为万为单位保留一位小数
     * @param _count
     * @return
     */
    public static String changeCountFormat(String _count){
        int _ct= Integer.valueOf(_count).intValue();
        String countStr="";
        float _cf=0;
        if(_ct<10000){
            return _count;
        }else{
            _cf=((float) _ct)/ 10000;
            countStr= String.format("%.1f", _cf)+"万";
        }
        return countStr;
    }

    /**
     * 格式化文件大小(参数的单位是kb)
     * @param _s
     * @return
     */
    public static String changeSizeFormat(int _s){
        int _ct= Integer.valueOf(_s).intValue();
        String _Str="";
        float _cf=0;
        if(_ct<1024){
            return _s+"KB";
        }else{
            _cf=((float) _ct)/1024;
            _Str= String.format("%.1f", _cf)+"MB";
        }
        return _Str;
    }

    /**
     * 计算时间差
     * @param startDate
     * @param endDate
     * @return
     */
    public static String DateDistance(Date startDate, Date endDate){
        if(startDate == null ||endDate == null){
            return null;
        }
        long timeLong = endDate.getTime() - startDate.getTime();
        if(timeLong<0){
            timeLong=0;
        }
        if (timeLong<60*1000)
            return timeLong/1000 + "秒前";
        else if (timeLong<60*60*1000){
            timeLong = timeLong/1000 /60;
            return timeLong + "分钟前";
        }
        else if (timeLong<60*60*24*1000){
            timeLong = timeLong/60/60/1000;
            return timeLong+"小时前";
        }
        else if ((timeLong/1000/60/60/24)<7){
            timeLong = timeLong/1000/ 60 / 60 / 24;
            return timeLong + "天前";
        }else{
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return formatter.format(startDate);
        }
//        else if ((timeLong/1000/60/60/24)<30){
//            timeLong = timeLong/1000/ 60 / 60 / 24/7;
//            return timeLong + "周前";
//        }
//        else if ((timeLong/1000/60/60/24/30)<12){
//            timeLong = timeLong/1000/ 60 / 60 / 24/30;
//            return timeLong + "月前";
//        }
//        else {
//            return timeLong/1000/60/60/24/30/12+"年前";
//        }
    }

    /**
     * 计算与当前的时间差
     * @param _ms
     * @return
     */
    public static String DateDistance2now(long _ms){
        SimpleDateFormat DateF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Long time=new Long(_ms);
            String d = DateF.format(time);
            Date startDate=DateF.parse(d);
            Date nowDate = Calendar.getInstance().getTime();
            return DateDistance(startDate, nowDate);
        }catch (Exception e){
        }
        return null;
    }
}
