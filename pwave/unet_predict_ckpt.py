from __future__ import print_function

from obspy import UTCDateTime
import numpy as np
import tensorflow as tf
import unet
import os
import backend.config as config

os.environ['KMP_DUPLICATE_LIB_OK'] = 'True'
cfg = config.Config()


"""
python3.6 + tensorflow1.12
"""


def fetch_window_data(stream, j):
    """fetch data from a stream window and dump in np array"""
    data = np.empty((3001, j))
    for i in range(j):
        data[:, i] = stream[i].data.astype(np.float32)
    data = np.expand_dims(data, 0)
    return data


def group_consecutives(vals, step=1):
    """Return list of consecutive lists of numbers from vals (number list)."""
    run = []
    result = [run]
    expect = None
    for v in vals:
        if (v == expect) or (expect is None):
            run.append(v)
        else:
            run = [v]
            result.append(run)
        expect = v + step
    return result


def predict(stream):
    checkpoint_path = cfg.unet_checkpoint_path
    batch_size = cfg.unet_batch_size
    image_size = cfg.unet_image_size

    samples = {
        'data': tf.placeholder(tf.float32,
                               shape=(batch_size, image_size, 3),
                               name='input_data')}
    with tf.Session() as sess:
        # 初始化model
        logits = unet.build_30s(samples['data'], 3, False)

        init_op = tf.group(tf.global_variables_initializer(), tf.local_variables_initializer())
        # writer = tf.summary.FileWriter("logs/", sess.graph)
        sess.run(init_op)
        saver = tf.train.Saver()

        if not tf.gfile.Exists(checkpoint_path + '.meta'):
            raise ValueError("Can't find checkpoint file")
        else:
            print('[INFO    ]\tFound checkpoint file, restoring model.')
            saver.restore(sess, checkpoint_path)
        coord = tf.train.Coordinator()

        threads = tf.train.start_queue_runners(sess=sess, coord=coord)

        # 处理数据
        predicted_images = unet.predict(logits, batch_size, image_size)
        to_fetch = [predicted_images, samples['data']]

        feed_dict = {samples['data']: fetch_window_data(stream.copy().normalize(), 3)}

        predicted_images_value, images_value = sess.run(to_fetch, feed_dict)

        clusters_p = np.where(predicted_images_value[0, :] == 1)
        p_boxes = group_consecutives(clusters_p[0])
        tp = []
        tpstamp = []

        if len(p_boxes) > 1:
            for ip in range(len(p_boxes)):
                tpmean = float(min(p_boxes[ip]) / 200.00 + max(p_boxes[ip]) / 200.00)
                tp.append(tpmean)
                tpstamp = UTCDateTime(stream[0].stats.starttime + tpmean).timestamp
                # print(UTCDateTime(tpstamp))

        # 绘制P波拾取图片
        win_filtered = stream.copy()
        lab = win_filtered[2].copy()
        lab.stats.channel = "LAB"
        # lab =win[0].copy()

        print("predicted_images_value", predicted_images_value.shape)
        lab.data[...] = predicted_images_value[0, :]
        win_filtered += lab
        # win_filtered[-2:].plot()
        win_filtered.plot()

        # Wait for threads to finish.
        coord.join(threads)
        print("pwave timestamp: ", tpstamp)
        print("pwave UTCtime: ", UTCDateTime(tpstamp))

        return tpstamp
