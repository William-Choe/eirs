import io
import os
import logging
import config
import shutil

from kafka import KafkaConsumer
from obspy import read, UTCDateTime


def main():
    # 初始化目录
    data_path = config.data_path
    if not os.path.exists(data_path):
        os.makedirs(data_path)
    # else:
    #     # 若存在data目录，清空数据
    #     shutil.rmtree(data_path)
    #     os.makedirs(data_path)

    # 初始化Kafka
    consumer = KafkaConsumer(config.data_topic,
                             group_id="eirs_test_mock",
                             bootstrap_servers=config.data_bootstrap_servers,
                             enable_auto_commit=True,
                             auto_offset_reset="latest")
    print("Subscribe Topic:", config.data_topic)
    for message in consumer:
        if message:
            try:
                # 过滤数据，只保留50HZ和100HZ数据包
                stream = read(io.BytesIO(message.value))
                if stream[0].stats.sampling_rate == 50 or stream[0].stats.sampling_rate == 100:
                    coordinate = stream[0].stats.network + "." + stream[0].stats.station + "." + stream[0].stats.location
                    channel = stream[0].stats.channel
                    start_time = UTCDateTime(stream[0].stats.starttime).strftime("%Y%m%d%H%M%S")
                    mseed_path = os.path.join(os.path.join(data_path, coordinate), channel)
                    label = coordinate + "." + channel + "." + start_time + ".mseed"
                    if not os.path.exists(mseed_path):
                        os.makedirs(mseed_path)
                    stream.write(os.path.join(mseed_path, label), format='MSEED')
            except Exception as e:
                logging.error(e)


if __name__ == '__main__':
    main()
