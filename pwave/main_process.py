import os
import datetime
import time
import config
import pytz
import shutil

from multiprocessing import Process
from collections import defaultdict
from obspy import UTCDateTime, Stream, read
from bisect import bisect_left
from unet_predict_pb import predict
from data_to_kafka import sendPWave

os.environ["TF_CPP_MIN_LOG_LEVEL"] = '3'


# 查找与当前时间最近的数据包
def takeClosestMSEED(t, mseeds):
    times = [int(i[-20:-6]) for i in mseeds]
    seed_idx = bisect_left(times, t)
    if seed_idx >= len(times):
        seed_idx = len(times) - 1
    return seed_idx


# 按照计算进程数量切分stations
def splitStations(sta_set, ps_num):
    split_sta = []
    sta_list = list(sta_set)
    n = len(sta_list) // ps_num
    sta_list_split = [sta_list[i:i+n] for i in range(0, len(sta_list), n)]

    # 若切分数量大于进程数，将后两个list合并
    if len(sta_list_split) > ps_num:
        sta_list_split[-2].extend(sta_list_split[-1])
        sta_list_split.pop()
    for sl in sta_list_split:
        split_sta.append(sl)

    return split_sta


# 处理并计算各台站波形
def processStations(stations, cur_time, data_path):
    for station in stations:
        # print("\n-> Start Processing Station:", station)
        station_path = os.path.join(data_path, station)
        channels = os.listdir(station_path)

        '''
        存储各个channel中与当前时间指针最接近的MSEED
        channel_mseed_dic:
          key: channel
          value: [idx, seeds]
              idx表示seeds中最接近当前时间指针的MSEED下标
              seeds为当前channel中全部MSEED，按时间顺序排列
        '''
        channel_mseed_dic = defaultdict(list)
        for channel in channels:
            seed_path = os.path.join(station_path, channel)
            mseeds = os.listdir(seed_path)
            mseeds.sort()
            # print(station, channel, mseeds)
            idx = takeClosestMSEED(cur_time, mseeds)
            channel_mseed_dic[channel] = [idx, mseeds]

        # 读取MSEED，拼接数据
        stream = Stream()
        for channel, value in channel_mseed_dic.items():
            seed_path = os.path.join(station_path, channel)
            current_index = value[0]
            mseeds = value[1]

            # 读取与当前时间戳最近的数据包
            trace_closest = read(os.path.join(seed_path, mseeds[current_index]))
            stream += trace_closest
            for idx in reversed(range(current_index)):
                # 向前读取不超过五个数据包
                if current_index - idx > 8:
                    break

                # 若该MSEED EndTime 与 下一个MSEED StartTime 时间间隔大于0.02，判定其为断迹，停止拼接
                trace = read(os.path.join(seed_path, mseeds[idx]))
                # trace_next = read(os.path.join(seed_path, mseeds[idx + 1]))
                # if trace_next[0].stats.starttime - trace[0].stats.endtime > 0.02:
                #     # print("-> Skip fault trace!")
                #     break
                stream += trace
        stream.merge(fill_value='latest')

        # 确定窗口右侧临界时间（取BHE、BHN、BHZ endTime最小值）
        window_right = min([tr.stats.endtime for tr in stream])
        # print("-> Determine right time of window:", window_right)

        # 创建窗口
        # print("-> Create window stream")
        win_stream = Stream()
        if stream[0].stats.sampling_rate == 50:
            win_stream = stream.slice(window_right - 60, window_right)
        if stream[0].stats.sampling_rate == 100:
            win_stream = stream.slice(window_right - 30, window_right)

        # 发现断迹或缺少频道，跳过该台站
        if len(win_stream) < 3:
            continue

        # 若某一channel采样点数小于3001个点，跳过该台站
        if min([tr.stats.npts for tr in win_stream]) != 3001:
            continue

        # 预处理波形
        win_stream.normalize()
        win_stream.detrend('constant')
        win_stream.filter('bandpass', freqmin=0.5, freqmax=20)

        # 拾取P波
        p_timestamp = predict(win_stream)
        # 拾取到P波，运行后续流程
        if p_timestamp:
            sendPWave("p-wave-info", station + ":" + str(p_timestamp))
            print("-> Coordinates:", station + ", P-wave arrival time:", UTCDateTime(p_timestamp))


def main():
    utc_tz = pytz.timezone('UTC')
    data_path = config.data_path
    process_num = int(os.cpu_count())

    # 预热数据2分钟
    print("Data Preheating 120s...")
    # time.sleep(120)
    print("Done!")

    current_datetime = datetime.datetime.now(utc_tz) + datetime.timedelta(minutes=-1)
    current_time = int(datetime.datetime.strftime(current_datetime, '%Y%m%d%H%M%S'))
    while True:
        start_time = datetime.datetime.now()
        print("\nCurrent time:", str(current_time))

        # 获取全部接收到数据的台站，去除问题台站（缺少channel）
        stations = os.listdir(data_path)
        print("Original station number:", len(stations))
        for sta in stations.copy():
            sta_path = os.path.join(data_path, sta)
            if len(os.listdir(sta_path)) < 3:
                # print("Skip processing station:", sta, str(os.listdir(sta_path)))
                stations.remove(sta)
        print("Filtered station number:", len(stations))

        # 切分stations
        stations_list = splitStations(stations.copy(), process_num)
        process_list = []
        for stations in stations_list:
            p = Process(target=processStations, args=(stations, current_time, data_path,))
            process_list.append(p)

        # 开启全部计算任务
        for process in process_list:
            process.start()

        # 等待该轮计算任务结束
        for process in process_list:
            process.join()

        end_time = datetime.datetime.now()
        print("Computing Time:", end_time - start_time)

        # 更新时间，滑动窗口30s
        current_datetime = current_datetime + datetime.timedelta(seconds=30)
        current_time = int(datetime.datetime.strftime(current_datetime, '%Y%m%d%H%M%S'))


if __name__ == '__main__':
    main()
