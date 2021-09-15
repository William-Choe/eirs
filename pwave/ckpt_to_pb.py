from tensorflow.python.framework.graph_util import convert_variables_to_constants
from tensorflow.python.framework import graph_io
import unet

import tensorflow.compat.v1 as tf
tf.disable_v2_behavior()


"""
运行以下代码将ckpt转换为pb模型
"""
pb_file = "unet_590000.pb"
ckpt_file = "./model/unet.ckpt-590000"
output_node_names = ["input_data", "Reshape"]
samples = {'data': tf.placeholder(tf.float32, shape=(1, 3001, 3), name='input_data')}

# 使用placeholder留一个输入接口
with tf.name_scope('input'):
    input_data = samples['data']
model = unet.build_30s(samples['data'], 3, False)

sess = tf.Session(config=tf.ConfigProto(allow_soft_placement=True))
saver = tf.train.Saver()
saver.restore(sess, ckpt_file)

frozen_graph = convert_variables_to_constants(sess, sess.graph_def, output_node_names)

graph_io.write_graph(frozen_graph, './model', pb_file, as_text=False)
