package pojo;

public class EarthQuakeMapper {

	private int id;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private byte[] value;//512B

    private double time;//BTime

    private String station;//台网台站名
    
    private String channelId;

    private String servername;//服务器名字

    private double nowtime;//接受到数据时的当前系统时间

    
    
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public String getServername() {
		return servername;
	}

	public void setServername(String servername) {
		this.servername = servername;
	}

	public double getNowtime() {
		return nowtime;
	}

	public void setNowtime(double nowtime) {
		this.nowtime = nowtime;
	}

	

	public EarthQuakeMapper(byte[] value, double time, String station,
                            String channelId, String servername, double nowtime) {
		super();
		this.value = value;
		this.time = time;
		this.station = station;
		this.channelId = channelId;
		this.servername = servername;
		this.nowtime = nowtime;
	}

	public EarthQuakeMapper() {
		super();
	}
    
}
