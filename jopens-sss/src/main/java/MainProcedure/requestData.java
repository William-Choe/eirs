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

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.regex.Pattern;


public class requestData {
    private static KafkaProducer<String, byte[]> producer = KafkaProducerService.createKafkaProducer("config.properties");
    private static String topic = "data-6.0";
    private static HashSet<String> keySet = new HashSet<>();


    private static String netNamePath;
    private static String filePath;
    private static ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
        public String handleResponse(final HttpResponse httpResponse) throws ClientProtocolException, IOException {
            HttpEntity entity = httpResponse.getEntity();
            String str = null;
            String Lasttimes = null;
            String LastfileName = null;
            Header contentType = entity.getContentType();
            String[] cont = contentType.getValue().split(";");
            String code = cont[0];
            try {
                if (code.equals("text/plain")) {
                    str = EntityUtils.toString(entity, "utf-8");
                    System.out.println("str:" + str);
                } else {

                    int len  = 256;
                    byte[] bytes = new byte[len];
                    byte[] Lastbytes = new byte[len];

                    int read = 0;
                    int off = 0;
                    InputStream content = entity.getContent();
                    int count = 0;
                    while (true) {
                        //String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
                        read = 0;
                        off  = 0;
                        while (off < len && (read = content.read(bytes, off, len - off)) != -1) {
                            off += read;
                        }
                        if(read == -1) break;
                        String netName = MSUtil.getNetname(bytes);
                        String stations = MSUtil.getStation(bytes).trim();
                        String channels = MSUtil.getChannelID(bytes);
                        String zero = MSUtil.getZero(bytes);
                        String times = MSUtil.getStrTime(bytes);
                        String fileName = netName+"."+stations+"."+zero+"."+channels+"."+times+".mseed";
                        Pattern p = Pattern.compile("[a-zA-Z]");
                        if(!(p.matcher(netName).find()&&p.matcher(channels.substring(0,1)).find()&&p.matcher(stations).find())) {

                            createFile(filePath,LastfileName,Lastbytes,Lasttimes,"512");
                            createFile(filePath,LastfileName,bytes,Lasttimes,"512");
                            count = 0;
                            continue;
                        }

                    if (count==1){
                        createFile(filePath,LastfileName,Lastbytes,Lasttimes,"256");

                    }else{
                        count = 1;
                    }
                    LastfileName = fileName;
                    Lasttimes = times+"-";
                    for(int ii = 0;ii < len;ii++){
                        Lastbytes[ii] = bytes[ii];
                    }

                    }
                }


            } catch (Exception e) {
                System.out.println(e);
            }
            return str;
        }
    };

    private static void send(String topic, String key, byte[] value,KafkaProducer<String, byte[]> producer) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(topic, key, value);
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

    public static String Get(CloseableHttpClient httpclient, String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Connection", "keep-alive");
        httpGet.setProtocolVersion(HttpVersion.HTTP_1_0);
        httpGet.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
        httpGet.addHeader("Range","bytes = 1500000");
        System.out.println("请求信息：" + httpGet.getRequestLine());
        String entity = null;
        try {
            entity = httpclient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            return entity;
        }
    }
    public static String Post(CloseableHttpClient httpclient, String url, String staList) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Connection", "keep-alive");
        httpPost.setProtocolVersion(HttpVersion.HTTP_1_0);
        httpPost.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
        httpPost.addHeader("Range","bytes = 1500000");
        System.out.println("请求信息：" + httpPost.getRequestLine());
        String entry = null;
        Map<String,String> map=new HashMap<>();
        map.put("staList",staList);
        StringEntity stringEntity = new StringEntity(map.toString(), Charset.forName("UTF-8"));
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        try {
            entry = httpclient.execute(httpPost, responseHandler);
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            return entry;
        }
    }
    public static void main(String[] args) throws Exception {

        XMLConfig xmlConfig = GetCongfig.dowith();
        String IP = xmlConfig.getIP();
        String Port = xmlConfig.getPort();
        String UserName = xmlConfig.getUserName();
        String PassWord = xmlConfig.getPassWord();
        filePath = xmlConfig.getFilePath();
        netNamePath = xmlConfig.getNetNamePath();
        String[] strings = xmlConfig.getStaList().split(" ");
        for (String s: strings ) {
            keySet.add(s);
        }
        String staList = Utils.urlEncode(xmlConfig.getStaList());
        String ChanMask = xmlConfig.getChanMask();
        String sn = xmlConfig.getSn();

        String url = "http://" + IP + ":" + Port + "/jopens-sss/sss/retr;staList=.*;";
        if (ChanMask != "") {
            url += "chanMask=" + ChanMask + ";";
        }
        if (sn != "") {
            url += "sn=" + sn + ";";
        }
        CloseableHttpClient http = HttpClients.createDefault();
        try {
            System.out.println("Get from a...");
            String a = Get(http, "http://" + IP + ":" + Port + "/jopens-sss/sss/login;user=" + UserName + ";pass=" + PassWord + ";");
            System.out.println("Get from b...");
            String b = Get(http, url);
        } catch (Exception exception) {
            String c = Get(http, "http://10.5.202.30:8080/jopens-sss/sss/logout;");
        }
    }

    public static synchronized boolean createFile(String strpath, String fileName, byte[] data, String times, String byteNum) {
        Boolean bool = false;
        fileName = fileName.replaceAll(" +","") ;
        String[] date = times.split("000000-");
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);//获取年份
        String yearKey = Integer.toString(year);
        if(!date[0].contains(yearKey)){
            return true;
        }
        strpath = strpath + "/"+byteNum+"/" + date[0];
        File file1 = new File(strpath);
        if (!file1.exists()){
            file1.mkdirs();
        }
        File file = new File((strpath +"/"+ fileName));
        try {
            if (!file.exists()) {

                file.createNewFile();
                bool = true;
                byte2file(strpath +"/"+ fileName, data);
            } else {
                byte2file(strpath +"/"+ fileName, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bool;
    }


    public static void byte2file(String path, byte[] data) {
        try {

            FileOutputStream outputStream = new FileOutputStream(new File(path), true);
            outputStream.write(data);
            outputStream.close();
            //System.out.println("success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
