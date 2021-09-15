package pojo;

public class XMLConfig {
    String IP;
    String Port;
    String UserName;
    String PassWord;
    String staList;
    String chanMask;
    String sn;
    String filePath;
    String netNamePath;

    public String getNetNamePath() {
        return netNamePath;
    }

    public void setNetNamePath(String netNamePath) {
        this.netNamePath = netNamePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getPort() {
        return Port;
    }

    public void setPort(String port) {
        Port = port;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassWord() {
        return PassWord;
    }

    public void setPassWord(String passWord) {
        PassWord = passWord;
    }

    public String getStaList() {
        return staList;
    }

    public void setStaList(String staList) {
        this.staList = staList;
    }

    public String getChanMask() {
        return chanMask;
    }

    public void setChanMask(String chanMask) {
        this.chanMask = chanMask;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
