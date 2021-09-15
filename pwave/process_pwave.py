import os
import logging
import datetime
import sys
from bisect import bisect_left

from collections import defaultdict
from kafka import KafkaConsumer
from obspy import UTCDateTime, Stream, read
from data_to_kafka import sendMsg
import database
import config


# 将Stream解析成json字符串
def parseStream(stream, timestamp, catalog_id):
    dic = defaultdict()
    dic['db_id'] = catalog_id
    dic['station'] = stream[0].get_id()[:-4]
    dic['start_time'] = str(stream[0].stats.starttime)
    dic['pwave_timestamp'] = timestamp
    dic['pwave_index'] = int(UTCDateTime(timestamp - stream[0].stats.starttime.timestamp).timestamp * 100)
    for trace in stream:
        channel = trace.get_id()[-3:]
        dic[channel]['data'] = trace.data.tolist()
    return dic


def saveStream(stream, path):
    station_path = path + stream[0].get_id()[:-3]
    label = stream[0].get_id()[:-3] + UTCDateTime(stream[0].stats.starttime).strftime("%Y%m%d%H%M%S") + ".mseed"
    output_mseed_path = os.path.join(station_path, label)
    if not os.path.exists(station_path):
        os.makedirs(station_path)
    stream.write(output_mseed_path, format='MSEED')


def takeClosestMSEED(t, mseeds):
    times = [int(i[-20:-6]) for i in mseeds]
    seed_idx = bisect_left(times, t)
    if seed_idx >= len(times):
        seed_idx = len(times) - 1
    return seed_idx


def main():
    consumer = KafkaConsumer(config.eirs_topic_process_pwave,
                             group_id=config.eirs_group_id_process_pwave,
                             enable_auto_commit=True,
                             auto_offset_reset="latest",
                             bootstrap_servers=config.eirs_bootstrap_servers)
    print("Subscribe Topic:", config.eirs_topic_process_pwave)
    p_waves = defaultdict(list)
    db = database.DB()
    data_path = config.data_path

    for message in consumer:
        if message:
            try:
                # 若该P波到时与上一个到时差小于30秒则舍弃
                station, p_timestamp = bytes.decode(message.value, encoding="utf-8").split(":")
                p_timestamp = float(p_timestamp)
                print(station)
                print(UTCDateTime(p_timestamp))

                if p_waves[station] and p_timestamp - p_waves[station][-1] <= 30:
                    print("Skip this p wave, <30s!")
                    continue
                p_waves[station].append(p_timestamp)

                # 按照P波到时重新构建波形
                station_path = os.path.join(data_path, station)
                channels = os.listdir(station_path)

                channel_mseed_dic = defaultdict(list)
                for channel in channels:
                    seed_path = os.path.join(station_path, channel)
                    mseeds = os.listdir(seed_path)
                    mseeds.sort()
                    p_datetime = int(UTCDateTime(p_timestamp).strftime('%Y%m%d%H%M%S'))
                    idx = takeClosestMSEED(p_datetime, mseeds)
                    channel_mseed_dic[channel] = [idx, mseeds]

                stream = Stream()
                for channel, value in channel_mseed_dic.items():
                    seed_path = os.path.join(station_path, channel)
                    p_seed_index = value[0]
                    mseeds = value[1]

                    # 读取与当前时间戳最近的数据包
                    for idx in range(p_seed_index - 2, len(mseeds)):
                        # 向后读取不超过五十个数据包
                        if idx - p_seed_index > 30:
                            break
                        trace = read(os.path.join(seed_path, mseeds[idx]))
                        stream += trace
                stream.merge(fill_value='latest')
                print(stream)

                stream_cut = Stream()
                if stream[0].stats.sampling_rate == 50:
                    stream_cut = stream.slice(UTCDateTime(p_timestamp), UTCDateTime(p_timestamp) + 200)
                if stream[0].stats.sampling_rate == 100:
                    stream_cut = stream.slice(UTCDateTime(p_timestamp), UTCDateTime(p_timestamp) + 100)
                print(stream_cut)

                if min([tr.stats.npts for tr in stream_cut]) < 10000:
                    continue

                catalog_id = db.insert_p(station, p_timestamp)
                stream_json = parseStream(stream_cut, p_timestamp, catalog_id)
                sendMsg("eq_warn", stream_json)
                saveStream(stream_cut, config.pwave_save_path)
            except Exception as e:
                exc_type, exc_value, exc_traceback = sys.exc_info()
                logging.error(exc_traceback)


if __name__ == '__main__':
    main()
