package tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pojo.XMLConfig;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;

public class GetCongfig {

    /*public static void main(String[] args) throws Exception{
        dowith();
    }*/

    public static XMLConfig dowith()throws Exception{
        String fileName=System.getProperty("user.dir")+"/configuration.xml";
        System.out.println(fileName);
        XMLConfig xmlConfig=readXmlFile(fileName);
        return xmlConfig;
    }
    public static XMLConfig readXmlFile(String fileName) throws Exception {
        XMLConfig xmlConfig=new XMLConfig();
        FileInputStream xmlInputStream = new FileInputStream(fileName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // 返回documentBuilderFactory对象
        DocumentBuilder db = dbf.newDocumentBuilder();// 返回db对象用documentBuilderFatory对象获得返回documentBuildr对象
        Document dt = db.parse(xmlInputStream); // 得到一个DOM并返回给document对象
        Element element = dt.getDocumentElement();// 得到一个elment根元素
        System.out.println("根元素：" + element.getNodeName()); // 获得根节点
        NodeList childNodes = element.getChildNodes(); // 获得根元素下的子节点
        for (int i = 0; i < childNodes.getLength(); i++) // 遍历这些子节点
        {
            Node node1 = childNodes.item(i); // childNodes.item(i);
            // 获得每个对应位置i的结点
            if ("Message".equals(node1.getNodeName())) {


                NodeList nodeDetail = node1.getChildNodes(); // 获得<Accounts>下的节点
                for (int j = 0; j < nodeDetail.getLength(); j++) { // 遍历<Accounts>下的节点
                    Node detail = nodeDetail.item(j); // 获得<Accounts>元素每一个节点
                    if ("IP".equals(detail.getNodeName())){
                        xmlConfig.setIP(detail.getTextContent());
//                        System.out.println("IP: " + detail.getTextContent());
                    }

                    else if ("Port".equals(detail.getNodeName())){
                        xmlConfig.setPort(detail.getTextContent());
//                        System.out.println("Port: " + detail.getTextContent());
                    }

                    else if ("UserName".equals(detail.getNodeName())){
                        xmlConfig.setUserName(detail.getTextContent());
//                        System.out.println("UserName: " + detail.getTextContent());
                    }

                    else if ("PassWord".equals(detail.getNodeName())){
                        xmlConfig.setPassWord(detail.getTextContent());
//                        System.out.println("PassWord: " + detail.getTextContent());
                    }
                    else if ("staList".equals(detail.getNodeName())){
                        xmlConfig.setStaList(detail.getTextContent());
//                        System.out.println("staList: " + detail.getTextContent());
                    }
                    else if("chanMask".equals(detail.getNodeName())){
                        xmlConfig.setChanMask(detail.getTextContent());
                    }
                    else if("sn".equals(detail.getNodeName())){
                        xmlConfig.setSn(detail.getTextContent());
                    }
                    else if("FilePath".equals(detail.getNodeName()) ){
                        xmlConfig.setFilePath(detail.getTextContent());
                    }
                    else if("NetNamePath".equals(detail.getNodeName()) ){
                        xmlConfig.setNetNamePath(detail.getTextContent());
                    }

                }
            }
        }
        return xmlConfig;
    }
}
