package tools;

import java.util.Arrays;
import java.util.Calendar;

//时间工具类,用于解析时间
public class MSUtil {
    //获得所有相关信息
    public static String getTotal(byte[] bytes) {
        return MSUtil.getNetname(bytes) + "." + MSUtil.getStation(bytes)
                + "." + MSUtil.getChannelID(bytes) + "." + MSUtil.getSecondTime(bytes);
    }

    //如名(精确到秒)
    public static double getTime(byte[] value) {
        byte[] time;
        time = Arrays.copyOfRange(value, 20, 27);
        short yearh = Utils.getUint8(time[0]);
        short yearl = Utils.getUint8(time[1]);
        String year = String.valueOf(yearh * 256 + yearl);
        short dayh = Utils.getUint8(time[2]);
        short dayl = Utils.getUint8(time[3]);
        String day = String.valueOf(dayh * 256 + dayl);


        String hour = String.valueOf(Utils.getUint8(time[4]));
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minute = String.valueOf(Utils.getUint8(time[5]));
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String second = String.valueOf(Utils.getUint8(time[6]));
        if (second.length() == 1) {
            second = "0" + second;
        }
        String Day = getday(year, day, hour);
        String timeString = year + "" + Day + "" + hour + "" + minute + "" + second;
        return Double.parseDouble(timeString);

    }

    //getTime的String版本(精确到天)
    public static String getStrTime(byte[] value) {

        byte[] time;
        time = Arrays.copyOfRange(value, 20, 27);
        short yearh = Utils.getUint8(time[0]);
        short yearl = Utils.getUint8(time[1]);
        String year = String.valueOf(yearh * 256 + yearl);
        short dayh = Utils.getUint8(time[2]);
        short dayl = Utils.getUint8(time[3]);
        String day = String.valueOf(dayh * 256 + dayl);


        String hour = String.valueOf(Utils.getUint8(time[4]));
        if (hour.length() == 1) {
            hour = "0" + hour;
        }

        String minute = String.valueOf(Utils.getUint8(time[5]));
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String second = String.valueOf(Utils.getUint8(time[6]));
        if (second.length() == 1) {
            second = "0" + second;
        }
        String Day = getday(year, day, hour);
        return year + "" + Day + "000000";

    }


    public static String getSecondTime(byte[] value) {

        byte[] time;
        time = Arrays.copyOfRange(value, 20, 27);
        short yearh = Utils.getUint8(time[0]);
        short yearl = Utils.getUint8(time[1]);
        String year = String.valueOf(yearh * 256 + yearl);
        short dayh = Utils.getUint8(time[2]);
        short dayl = Utils.getUint8(time[3]);
        String day = String.valueOf(dayh * 256 + dayl);


        String hour = String.valueOf(Utils.getUint8(time[4]));
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minute = String.valueOf(Utils.getUint8(time[5]));
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String second = String.valueOf(Utils.getUint8(time[6]));
        if (second.length() == 1) {
            second = "0" + second;
        }

        //根据一年的第几天获取日期
        String Day = getDay(year, day, hour);

        return year + "" + Day + "000000" ;

    }

    // 根据第几天解析月日,16:00
    public static String getday(String Year, String Day, String hour) {
        int year = Integer.parseInt(Year);
        int day = Integer.parseInt(Day);
        String month = null, days = null;
        int d = 0;
        int m = 1;
        int[] Month = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 != 0) || ((year % 100 == 0) && (year % 400 != 0))) {
            //不是闰年，2月28天
        } else {
            Month[1] = 29;//是闰年，2月29天
        }
        for (int i = 0; i < 12; i++) {
            d = day - Month[i];
            if (d > 0) {
                day = d;
                m++;
            } else {
                d = d + Month[i];
                break;
            }
        }
        if (m < 10) {
            month = "0" + m;
        } else {
            month = "" + m;
        }

        //判断是否大于16:00
        int i = Integer.parseInt(hour);
        if (16 <= i && i <= 24) {
            d = d + 1;
        }

        if (d < 10) {
            days = "0" + d;
        } else {
            days = "" + d;
        }


        return month + days;
    }

    // 根据第几天解析月日
    public static String getDay(String Year, String Day, String hour) {
        int year = Integer.parseInt(Year);
        int day = Integer.parseInt(Day);
        String month = null, days = null;
        int d = 0;
        int m = 1;
        int[] Month = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 != 0) || ((year % 100 == 0) && (year % 400 != 0))) {
            //不是闰年，2月28天
        } else {
            Month[1] = 29;//是闰年，2月29天
        }
        for (int i = 0; i < 12; i++) {
            d = day - Month[i];
            if (d > 0) {
                day = d;
                m++;
            } else {
                d = d + Month[i];
                break;
            }
        }
        if (m < 10) {
            month = "0" + m;
        } else {
            month = "" + m;
        }


        if (d < 10) {
            days = "0" + d;
        } else {
            days = "" + d;
        }
        return month + days;
    }

    //将系统时间转为BTime，没有实现
    public static byte[] changeTime(byte[] bytes) {
        byte[] newbytes = null;
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return newbytes;
    }

    //解析台站
    public static String getStation(byte[] value) {
        byte[] station = Arrays.copyOfRange(value, 8, 13);
        String stations = Utils.byteToString(station);
        return stations.replaceAll(" ", "");

    }

    //解析台网名
    public static String getNetname(byte[] value) {
        byte[] head = Arrays.copyOfRange(value, 18, 20);
        return Utils.byteToString(head);
    }

    //解析频道
    public static String getChannelID(byte[] value) {
        byte[] head = Arrays.copyOfRange(value, 15, 18);
        return Utils.byteToString(head);

    }

    public static String getZero(byte[] value){
        byte[] zero = Arrays.copyOfRange(value,13,15);
        String zeros = Utils.byteToString(zero);
        return zeros;
    }

}
