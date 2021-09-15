from kafka import KafkaProducer
import json
import config


# 发送数据至Kafka
def sendMsg(topic, msg_dict):
    producer = KafkaProducer(bootstrap_servers=config.eirs_bootstrap_servers,
                             value_serializer=lambda v: json.dumps(v).encode('utf-8'))
    producer.send(topic, msg_dict)
    producer.close()


# 发送P波基本信息：
#   info = station:p_timestamp
def sendPWave(topic, message):
    producer = KafkaProducer(bootstrap_servers=config.eirs_bootstrap_servers)
    producer.send(topic, bytes(message, encoding='utf-8'))
    producer.close()
