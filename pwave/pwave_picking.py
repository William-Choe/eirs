from collections import defaultdict
from obspy import Trace, UTCDateTime, Stream, read
from obspy.core.trace import Stats
from backend.common.stream_to_kafka import sendMsg
import backend.common.database as database
from unet_predict_pb import predict
import os
import backend.config as config
import numpy as np

cfg = config.Config()
db = database.DB()


def loadStream(path):
    stream = read(path)
    stream.merge()

    return stream


def preprocessStream(stream):
    stream = stream.detrend('constant')
    stream = stream.filter('bandpass', freqmin=0.5, freqmax=20)

    return stream


# 将Stream解析成json字符串
def parseStream(stream, timestamp, p_id):
    dic = defaultdict()
    dic['id'] = p_id
    dic['network'] = stream[0].stats.network
    dic['station'] = stream[0].stats.station
    dic['pwave_timestamp'] = timestamp
    dic['pwave_index'] = int(UTCDateTime(timestamp - stream[0].stats.starttime.timestamp).timestamp * 100)

    for trace in stream:
        tr_id = trace.get_id()
        # dic[tr_id]['stats'] = json.dumps(trace.stats, default=lambda obj: obj.__dict__)
        dic[tr_id]['startTime'] = str(trace.stats.starttime)
        dic[tr_id]['endTime'] = str(trace.stats.endtime)
        dic[tr_id]['data'] = trace.data.tolist()

    return dic


# 截取p波到时前n秒后m秒波形
def cutStream(stream, tpstamp, pwave_before, pwave_after):
    dt = UTCDateTime(tpstamp)
    stream_cut = stream.slice(dt - pwave_before, dt + pwave_after)

    return stream_cut


# 将剪切的波形存储至本地
def saveCutStream(stream, path):
    output_mseed_dir = path
    output_label = "{}_{}.mseed".format(stream[0].stats.station,
                                        UTCDateTime(stream[0].stats.starttime).strftime("%Y%m%d%H%M%S"))
    output_mseed = os.path.join(output_mseed_dir, output_label)
    stream.write(output_mseed, format='MSEED')


def readData(path):
    with open(path, 'r') as file:
        data = [int(i) for i in file.read().splitlines()]
        return data


def initialStats(data_file):
    st_info = data_file.split('.')
    stats = Stats()
    stats.network = st_info[0]
    stats.station = st_info[1]
    stats.location = st_info[2]
    stats.channel = data_file[-21:-18]
    stats.sampling_rate = 100
    stats.npts = 22001
    stats.starttime = UTCDateTime(int(data_file[-18:-14]), int(data_file[-14:-12]),
                                  int(data_file[-12:-10]), int(data_file[-10:-8]),
                                  int(data_file[-8:-6]), int(data_file[-6:-4]))
    return stats


def main():
    # 暂时读取有p波的30s波形(100HZ, 3001samples)，模拟P波拾取
    stream_file = "./seed/PWU_20080720223030_30s.mseed"
    stream = loadStream(stream_file)
    stream = stream.normalize()

    print(" + Preprocess stream {}".format(stream))
    stream = preprocessStream(stream)
    print(" -- Stream is ready, starting detection")

    # 返回p波到时timestamp
    tpstamp = predict(stream)

    # 读取3分钟波形
    stream_original = loadStream("./seed/PWU20080720223000.mseed")

    # 截取p波到时前n秒后m秒波形
    pwave_before, pwave_after = db.getPWaveConfig()
    stream_cut = cutStream(stream_original, tpstamp, pwave_before, pwave_after)

    # 将stream解析成json
    dic = parseStream(stream_cut, tpstamp)

    # 向kafka发送消息
    sendMsg("pwave_test", dic)

    # 存储剪切后的波形
    saveCutStream(stream_cut, cfg.output_mseed)


# Run system with txt wave samples
def main_txt():
    # Get all directories' name (wave form timestamp)
    root_path = "/Users/cui/Downloads/txt/"
    dirs = os.listdir(root_path)
    dirs.sort()
    dirs.remove(".DS_Store")

    # dic = defaultdict(dict)
    # for dir in dirs:
    #     sample_files = os.listdir(root_path + dir)
    #     sample_files.sort()
    #     if ".DS_Store" in sample_files:
    #         sample_files.remove(".DS_Store")
    #     dic[dir] = sample_files

    # Begin traversing through dirs
    for time in dirs:
        # Get all txt files' name at current "time"
        data_files = os.listdir(root_path + time)
        data_files.sort()
        if ".DS_Store" in data_files:
            data_files.remove(".DS_Store")

        print("\n" + "*"*64)
        print("Start processing time:", time)
        print("Data files:", data_files)

        # Save all stations at the current time into a set
        sta_set = set([i[:-22] for i in data_files])
        print("Stations:", sta_set)

        # Transform txt data to stream object (Three component wave)
        streams = []
        for station in sta_set:
            stream = Stream()
            for data_file in data_files:
                if data_file.find(station) == 0:
                    stats = initialStats(data_file)
                    data = np.array(readData(root_path + time + "/" + data_file))
                    trace = Trace(data=data, header=stats)
                    stream.append(trace)
            streams.append(stream)
        for st in streams:
            print(st)

        # Slide window configurations
        window_size = 30
        step = 10
        start_time = UTCDateTime(streams[0][0].stats.starttime)
        end_time = UTCDateTime(streams[0][0].stats.endtime)
        print("Start time:", start_time)
        print("End time:", end_time)

        # Store p-wave arrival timestamp, if next timestamp - this timestamp < 30s, give up next timestamp
        pwaves = defaultdict(list)
        for station in sta_set:
            pwaves[station] = []

        # Start traversing streams by sliding windows
        while True:
            for stream in streams:
                win_st = stream.slice(start_time, start_time + window_size)
                station = win_st[0].id[:-4]

                # Preprocess stream
                win_st.normalize()
                win_st = win_st.detrend('constant')
                win_st = win_st.filter('bandpass', freqmin=0.5, freqmax=20)

                # Predict P-wave arrival time
                tpstamp = predict(win_st)
                print("Start time:", start_time, "Coordinates:", station, "timestamp", tpstamp)
                if tpstamp:
                    if pwaves[station] and tpstamp - pwaves[station][-1] < 30:
                        print("\nThe time interval between two P-waves < 30s, abandon it!\n")
                        continue
                    pwaves[station].append(tpstamp)
                    print("\n-> Coordinates:", station + ", P-wave arrival time:", UTCDateTime(tpstamp), "\n")

                    # # 获取P波到时前n秒后m秒配置
                    # pwave_before, pwave_after = db.getPWaveConfig()
                    # # 将P波信息存入database
                    # p_id = db.insert_p(station, tpstamp)
                    # # 按照配置截取波形
                    # stream_cut = cutStream(stream, tpstamp, pwave_before, pwave_after)
                    # # 将stream转化为json字符串
                    # stream_json = parseStream(stream_cut, tpstamp, p_id)
                    # # 将拾取到的P波发送至Kafka供后续系统使用
                    # sendMsg("eq_warn", stream_json)
                    # # 将拾取到的P波存储至本地
                    # saveCutStream(stream_cut, cfg.output_mseed)

            start_time = UTCDateTime(start_time + step)

            # Boundary conditions:
            if UTCDateTime(start_time + window_size) > end_time:
                break


if __name__ == '__main__':
    main_txt()
