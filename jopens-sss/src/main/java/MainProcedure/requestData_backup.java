package MainProcedure;

import KafkaService.KafkaProducerService;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import pojo.XMLConfig;
import tools.GetCongfig;
import tools.MSUtil;
import tools.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;


public class requestData_backup {
    private static final KafkaProducer<String, byte[]> producer = KafkaProducerService.createKafkaProducer("config.properties");
    private static final String topic = "data-6.0";
    private static final HashSet<String> keySet = new HashSet<>();
    private static final String key = "xxx";

    private static final ResponseHandler<String> responseHandler = httpResponse -> {
        HttpEntity entity = httpResponse.getEntity();
        try {
            InputStream content = entity.getContent();
            int len  = 512;
            int read, off;
            byte[] bytes = new byte[len];
            while (true) {
                read = 0;
                off  = 0;
                while (off < len && (read = content.read(bytes, off, len - off)) != -1) {
                    off += read;
                }
                if (read == -1) {
                    break;
                }
                send(topic, key, bytes, producer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    };

    private static void send(String topic, String key, byte[] value, KafkaProducer<String, byte[]> producer) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, value);
        producer.send(record, (metadata, e) -> {
            if (e != null) {
                Date date = new Date(System.currentTimeMillis());
                System.out.println(date + " : " + key + " send failure！");
                System.out.println("Send to topic partition offset: " + metadata);
                e.printStackTrace();
            }
        });
    }

    public static void Get(CloseableHttpClient httpclient, String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Connection", "keep-alive");
        httpGet.setProtocolVersion(HttpVersion.HTTP_1_0);
        httpGet.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
        httpGet.addHeader("Range", "bytes = 1500000");
        try {
            httpclient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        XMLConfig xmlConfig = GetCongfig.dowith();
        String IP = xmlConfig.getIP();
        String Port = xmlConfig.getPort();
        String UserName = xmlConfig.getUserName();
        String PassWord = xmlConfig.getPassWord();
        String ChanMask = xmlConfig.getChanMask();
        String sn = xmlConfig.getSn();
        String url = "http://" + IP + ":" + Port + "/jopens-sss/sss/retr;staList=.*;";
        if (!ChanMask.equals("")) {
            url += "chanMask=" + ChanMask + ";";
        }
        if (!sn.equals("")) {
            url += "sn=" + sn + ";";
        }

        CloseableHttpClient http = HttpClients.createDefault();
        try {
            // Identity Authentication
            Get(http, "http://" + IP + ":" + Port + "/jopens-sss/sss/login;user=" + UserName + ";pass=" + PassWord + ";");
            // Creating data channel
            Get(http, url);
        } catch (Exception exception) {
            // Logout
            Get(http, "http://" + IP + ":" + Port + "/jopens-sss/sss/logout;");
        }
    }
}
