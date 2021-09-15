package org.earthquake.Timer;

import org.earthquake.KafkaService.KafkaConsumerService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class FileCleaner {
    private static final long INTERVAL = 24 * 60 * 60 * 1000;

    /**
     * 在这个函数下写业务逻辑,每隔指定时间会执行这个函数
     *
     * @param dirPath 要清除的目录
     */
    public static void createClearFileTimer(String dirPath) {
        //获取明天凌晨的时间(Date)
        Date tomorrowTime = FileCleaner.getTomorrowTime();
        System.out.println("timer will start at " + tomorrowTime + " ...");
        //获得要清除的目录
        final File dir = new File(dirPath);
        if (dir.isDirectory()) {
            //目录存在 创建Timer 明天00:00执行后 每隔24小时再次执行
            new Timer("clearFileTimer").scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //获取前两天的Date
                    Date beforeDate = FileCleaner.getDaysBefore(2);
                    //获取关键字 例如20191204000000
                    String keyWord = new SimpleDateFormat("yyyyMMdd000000").format(beforeDate);
                    //获得该目录下所有文件
                    File[] files = dir.listFiles();

                    if (files == null) return;
                    for (File file : files) {
                        deleteFile(file, keyWord);
                    }

                }
            }, tomorrowTime, INTERVAL);
        }
    }

    //获取明天00:00的Date对象
    private static Date getTomorrowTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hour=Integer.parseInt(KafkaConsumerService.deleteTime.split(":")[0]);
        int minute=Integer.parseInt(KafkaConsumerService.deleteTime.split(":")[1]);

        //设置为明天对应时间
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    //获取前n天的Date对象
    private static Date getDaysBefore(int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //设置为前两天
        calendar.add(Calendar.DATE, -n);
        return calendar.getTime();
    }


    //根据keyWord删除文件
    private static void deleteFile(File file, String keyWord) {
        if (file.getName().contains(keyWord))
            file.delete();
    }

}
