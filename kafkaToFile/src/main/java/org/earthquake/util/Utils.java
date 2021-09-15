package org.earthquake.util;

/*
 * create by lwc on 2018/3/30
 */

import org.earthquake.pojo.EarthQuakeMapper;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;


//工具类
public class Utils {

    //URL编码
    public static String urlEncode(String url) {
        url = java.net.URLEncoder.encode(url);
        url = url.replaceAll("\\+", "%20");
        return url;
    }

    //单个byte转char型
    public static char byteToChar(byte b) {
        return (char) b;
    }

    //单个byte转c语言中的uint8
    public static short getUint8(byte abyte) {

        short s = (short) abyte;
        return (short) (s & 0x00ff);
    }

    //byte数组转字符数组
    public static String byteToString(byte[] bytes) {
        String nRcvString;
        int length = bytes.length;
        StringBuffer tStringBuf = new StringBuffer();


        for (int i = 0; i < length; i++) {
            tStringBuf.append((char) bytes[i]);
        }
        nRcvString = tStringBuf.toString();
        return nRcvString;
    }

    //整型转byte[4]
    public static byte[] intToByte(int n) {

        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    //将byte[4]转整型
    public static int byteToInt(byte[] bArr) {

        if (bArr.length != 4) {
            return -1;
        }

        return (int) ((((bArr[3] & 0xff) << 24)
                | ((bArr[2] & 0xff) << 16)
                | ((bArr[1] & 0xff) << 8)
                | ((bArr[0] & 0xff) << 0)));
    }

    //byte型转double
    public static double bytesToDouble(byte[] arr) {
        String string = byteToString(arr);
        return Double.valueOf(string);
    }

    //byte[]按UTF-8转char[]
    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    //得到本机公网IP
    public static String getLocalIP() {

        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ip;
    }

    //得到本机未占用端口号
    public static int getPort() {
        int i;
        for (i = 5001; i < 65536; i++) {
            try {
                DatagramSocket ds = new DatagramSocket(i);
                ds.close();
                break;
            } catch (SocketException e) {
            }
        }
        return i;
    }


    //得到当前系统时间
    public static double getCurrentTime() {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        String time = year + "" + month + "" + date + "." + hour + "" + minute + "" + second;
        return Double.valueOf(time);
    }

    public static String getYear(byte[] bytes) {

        byte[] time;
        time = Arrays.copyOfRange(bytes, 20, 27);
        short yearh = Utils.getUint8(time[0]);
        short yearl = Utils.getUint8(time[1]);
        String year = String.valueOf(yearh * 256 + yearl);
        short dayh = Utils.getUint8(time[2]);
        short dayl = Utils.getUint8(time[3]);
        String day = dayh * 256 + "" + dayl;
        if (day.length() == 1) {
            day = "00" + day;
        } else if (day.length() == 2) {
            day = "0" + day;
        }
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
        String timeString = year;
        return year;

    }

    public static EarthQuakeMapper getEarthQuakeMapper(byte[] by, String servername) {
        double time = MSUtil.getTime(by);
        String station = MSUtil.getStation(by);
        double nowtime = Utils.getCurrentTime();
        String channelId = MSUtil.getChannelID(by);
        EarthQuakeMapper earthQuakeMapper = new EarthQuakeMapper(by, time, station, channelId, servername, nowtime);
        return earthQuakeMapper;
    }
}
