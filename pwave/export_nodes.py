import os
import tensorflow.compat.v1 as tf
tf.disable_v2_behavior()


"""
输出pb文件输入和输出节点
"""

model_dir = './model/'
model_name = 'unet_590000.pb'
export_file = 'result.txt'


# 读取并创建一个图graph来存放Google训练好的Inception_v3模型（函数）
def create_graph():
    with tf.gfile.FastGFile(os.path.join(model_dir, model_name), 'rb') as f:
        # 使用tf.GraphDef()定义一个空的Graph
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(f.read())
        # Imports the graph from graph_def into the current default Graph.
        tf.import_graph_def(graph_def, name='')


# 创建graph
create_graph()

tensor_name_list = [tensor.name for tensor in tf.get_default_graph().as_graph_def().node]
result_file = os.path.join(model_dir, export_file)
with open(result_file, 'w+') as f:
    for tensor_name in tensor_name_list:
        f.write(tensor_name + '\n')
