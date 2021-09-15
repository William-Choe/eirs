import logging
import redis
import io
import os
import json

from kafka import KafkaConsumer
from obspy import UTCDateTime, Stream, read
from data_to_kafka import sendMsg


# 初始化Redis
pool = redis.ConnectionPool(host="10.5.117.233", port="6379", password="", db=0)
myRedis = redis.Redis(connection_pool=pool)
# 存储P波波形
pwave_path = "C:\\Users\\cui\\Desktop\\pwaves\\"
# 存储事件json文件
event_path = "C:\\Users\\cui\\Desktop\\events\\"


def process_data(event):
    event_property = {"event_id": event['ID'],
                      "detection_type": event['DetectionType'],
                      "latitude": event['Hypocenter']['Latitude'],
                      "longitude": event['Hypocenter']['Longitude']}
    pwaves = event['Data']
    for pwave in pwaves:
        pwave_dic = dict(pwave)
        coordinate = pwave_dic['ID'][39:-4]
        pick_time = float(pwave_dic['PickTime'])
        # pick_time = int(UTCDateTime.now().timestamp) - 200

        # 读取Redis Zset数据流
        stream = Stream()
        for channel in ["BHE", "BHN", "BHZ"]:
            key = "MSEED:" + coordinate + "." + channel
            waves = myRedis.zrangebyscore(key, pick_time - 50, pick_time + 200)
            for wave in waves:
                stream += read(io.BytesIO(wave))
        stream.merge(fill_value='interpolate')

        if len(stream) < 3:
            continue

        label = stream[0].get_id()[:-3] + UTCDateTime(pick_time).strftime("%Y%m%d%H%M%S") + ".mseed"
        event_root_path = pwave_path + event_property["event_id"]
        if not os.path.exists(event_root_path):
            os.makedirs(event_root_path)
        output_mseed_path = os.path.join(event_root_path, label)
        stream.write(output_mseed_path, format='MSEED')

        # 读取字节流，存储在dict
        with open(output_mseed_path, 'rb') as file:
            waveform = file.read()
            data = {"pick_time": pick_time, "waveform": waveform}
            event_property[coordinate] = data

    with open(event_path + event_property["event_id"] + ".json", 'w') as file:
        file.write(str(event_property))
    sendMsg("events", event_property["event_id"] + ".json")


def main():
    """
    Topics:
    - pick: triggers
    - event: sfevents
    - event: sforigins （没有震级）
    """
    consumer = KafkaConsumer("sfevents",
                             bootstrap_servers=["10.5.117.231:9092", "10.5.117.232:9092", "10.5.117.233:9092", "10.5.117.234:9092",
                                                "10.5.117.235:9092", "10.5.117.236:9092", "10.5.117.237:9092", "10.5.117.238:9092",
                                                "10.5.117.241:9092", "10.5.117.242:9092", "10.5.117.243:9092", "10.5.117.244:9092"],
                             value_deserializer=lambda m: json.loads(m.decode('utf-8')),
                             enable_auto_commit=True,
                             auto_offset_reset="latest",
                             group_id="eirs")
    for message in consumer:
        if message:
            try:
                process_data(dict(message.value))
                break
            except Exception as e:
                logging.error(e)


if __name__ == '__main__':
    if not os.path.exists(pwave_path):
        os.makedirs(pwave_path)
    if not os.path.exists(event_path):
        os.makedirs(event_path)
    main()
