package org.earthquake.MainProcedure;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.earthquake.KafkaService.KafkaConsumerService;
import org.earthquake.util.MSUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ConsumerTask {
    private static final String CONFIG_PATH = System.getProperty("user.dir") + "\\resource\\" + "consumer.properties";
    private static KafkaConsumer<String, byte[]> consumer = KafkaConsumerService.createKafkaConsumer(CONFIG_PATH);
    private static final List<String> TOPICS = Arrays.asList(KafkaConsumerService.topics);
    private static final String PATH = KafkaConsumerService.path;

    public static void main(String[] args) {
//        FileCleaner.createClearFileTimer(PATH);
        ConsumerTask.process();
    }

    public static void process() {
        consumer.subscribe(TOPICS);
        consumer.poll(10);
        Map<TopicPartition, OffsetAndMetadata> offsetMap = new HashMap<TopicPartition, OffsetAndMetadata>();
        Set<TopicPartition> assignment = consumer.assignment();
        consumer.seekToEnd(assignment);
        for (TopicPartition topicPartition : assignment) {
            long position = consumer.position(topicPartition);
            offsetMap.put(topicPartition, new OffsetAndMetadata(position));
            consumer.commitAsync(offsetMap, null);
        }

        System.out.println("subscribe topics: " + TOPICS + " ...");
        while (true) {
            ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(10);
            for (ConsumerRecord<String, byte[]> record : consumerRecords) {
                byte[] dataPack = record.value();
                String netname = MSUtil.getNetname(dataPack);
                String stations = MSUtil.getStation(dataPack);
                String channels = MSUtil.getChannelID(dataPack);
                String zero = MSUtil.getZero(dataPack);
                String times = MSUtil.getStrTime(dataPack);
                String filename = netname + "." + stations + "." + zero + "." + channels + "." + times + ".mseed";
                filename = filename.replaceAll("[\u0000]", "");
                filename = filename.replaceAll(" ","");
                File file = new File(PATH);
                if (!file.exists()) {
                    if (file.mkdirs())
                        System.out.println("mseed存储于 " + PATH  + "rawData...");
                }
                createFile(PATH + "rawData\\", filename, dataPack);

                // 筛选存储的数据包，调用python脚本

            }
        }
    }

    public static synchronized boolean createFile(String path, String fileName, byte[] dataPack) {
        boolean bool = false;
        File file = new File(path + fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
                bool = true;
                byte2file(path + fileName, dataPack);
            } else {
                byte2file(path + fileName, dataPack);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bool;
    }


    public static void byte2file(String path, byte[] dataPack) {
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(path), true);
            outputStream.write(dataPack);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
