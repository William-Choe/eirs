package KafkaService;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Kafka Producer Service Factory
 */
public class KafkaProducerService {
    /**
     * @param file property file
     * @return kafka producer
     */
    public static KafkaProducer<String,byte[]> createKafkaProducer(String file) {
        Properties kafkaProducerProps = new Properties();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            kafkaProducerProps.load(bufferedReader);
            return new KafkaProducer<String,byte[]>(kafkaProducerProps);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
