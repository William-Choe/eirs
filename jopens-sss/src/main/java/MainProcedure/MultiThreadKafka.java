package MainProcedure;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Date;

public class MultiThreadKafka extends Thread{

    private String topic;
    private String key;
    private byte[] bytes;
    private KafkaProducer<String, byte[]> producer;

    public MultiThreadKafka(String topic,String key,byte[] bytes,KafkaProducer<String, byte[]> producer){
        this.producer = producer;
        this.bytes = bytes;
        this.key = key;
        this.topic = topic;

    }

    @Override
    public void run() {
        ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(topic, key, bytes);
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception e) {
                if(e != null)
                {
                    Date date = new Date(System.currentTimeMillis());
                    System.out.println(date.toString()+" : "+key+" 发送失败！");
                    System.out.println("发往主题分区偏移: "+metadata);
                    e.printStackTrace();
                }
            }
        });
    }
}
