package org.earthquake.KafkaService;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Kafka Producer Service Factory
 */
public class KafkaConsumerService {

    public static String[] topics;
    public static String path;
    public static String deleteTime;
    /**
     * @param file property file
     * @return kafka producer
     */
    public static KafkaConsumer<String, byte[]> createKafkaConsumer(String file) {
        Properties kafkaConsumerProps = new Properties();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            System.out.println("reading configuration...");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            kafkaConsumerProps.load(bufferedReader);
            return new KafkaConsumer<String, byte[]>(kafkaConsumerProps);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            KafkaConsumerService.getConfiguration(kafkaConsumerProps);
            System.out.println("success!");
        }

    }

    public static void getConfiguration(Properties properties) {
        topics = ((String) properties.get("topics")).split(",");
        path = (String) properties.get("folder.path");
        deleteTime = (String) properties.get("deleteTime");
    }


}
