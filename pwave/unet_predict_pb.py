from obspy import UTCDateTime
import numpy as np
import tensorflow.compat.v1 as tf
import os
import backend.config as config

tf.disable_v2_behavior()

os.environ['KMP_DUPLICATE_LIB_OK'] = 'True'
cfg = config.Config()


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
    pb_file = cfg.unet_pd_path
    batch_size = cfg.unet_batch_size
    image_size = cfg.unet_image_size

    with tf.Graph().as_default():
        output_graph_def = tf.GraphDef()

        with open(pb_file, "rb") as f:
            output_graph_def.ParseFromString(f.read())
            tf.import_graph_def(output_graph_def, name="")

        with tf.Session() as sess:
            init = tf.global_variables_initializer()
            sess.run(init)

            # 定义输入和输出的张量名称
            input_data = sess.graph.get_tensor_by_name("input_data:0")
            output_tensor_name = sess.graph.get_tensor_by_name("Reshape:0")

            feed_dict = {input_data: fetch_window_data(stream.copy().normalize(), 3)}
            to_fetch = tf.reshape(tf.argmax(output_tensor_name, axis=1), [batch_size, image_size])

            predicted_images_value = sess.run(to_fetch, feed_dict)
            # print('-------')
            # print(predicted_images_value)
            # print([(i, v) for i, v in enumerate(predicted_images_value[0]) if v == 1])

            clusters_p = np.where(predicted_images_value[0, :] == 1)
            p_boxes = group_consecutives(clusters_p[0])
            tp = []
            tpstamp = []

            if len(p_boxes) > 1:
                for ip in range(len(p_boxes)):
                    tpmean = float(min(p_boxes[ip]) / 200.00 + max(p_boxes[ip]) / 200.00)
                    tp.append(tpmean)
                    tpstamp = UTCDateTime(stream[0].stats.starttime + tpmean).timestamp

            # 绘制P波拾取图片
            # win_filtered = stream.copy()
            # lab = win_filtered[2].copy()
            # lab.stats.channel = "LAB"
            # lab.data[...] = predicted_images_value[0, :]
            # win_filtered += lab
            # win_filtered.plot()

            # print("pwave timestamp: ", tpstamp)
            # print("pwave UTCtime: ", UTCDateTime(tpstamp))

            return tpstamp